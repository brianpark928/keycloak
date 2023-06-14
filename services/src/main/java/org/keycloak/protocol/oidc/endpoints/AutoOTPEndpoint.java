/*
 * Copyright 2023 eStorm Inc
 */

package org.keycloak.protocol.oidc.endpoints;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.keycloak.http.HttpRequest;
import org.keycloak.http.HttpResponse;
import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.Profile;
import org.keycloak.common.constants.ServiceAccountConstants;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.common.util.ResponseSessionTask;
import org.keycloak.constants.AdapterConstants;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.AuthenticationFlowResolver;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.grants.ciba.CibaGrantType;
import org.keycloak.protocol.oidc.grants.device.DeviceGrantType;
import org.keycloak.protocol.oidc.utils.AuthorizeClientUtil;
import org.keycloak.protocol.oidc.utils.OAuth2Code;
import org.keycloak.protocol.oidc.utils.OAuth2CodeParser;
import org.keycloak.protocol.oidc.utils.PkceUtils;
import org.keycloak.protocol.saml.JaxrsSAML2BindingBuilder;
import org.keycloak.protocol.saml.SamlClient;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.rar.AuthorizationRequestContext;
import org.keycloak.representations.idm.authorization.AuthorizationRequest.Metadata;
import org.keycloak.saml.common.constants.JBossSAMLConstants;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.common.util.DocumentUtil;
import org.keycloak.services.CorsErrorResponseException;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.Urls;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.ResourceOwnerPasswordCredentialsContext;
import org.keycloak.services.clientpolicy.context.ResourceOwnerPasswordCredentialsResponseContext;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.managers.ClientManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.Cors;
import org.keycloak.services.util.AuthorizationContextUtil;
import org.keycloak.services.util.DefaultClientSessionContext;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.keycloak.utils.ProfileHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.namespace.QName;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.text.SimpleDateFormat;
import java.util.Date;

//import static org.keycloak.utils.LockObjectsForModification.lockUserSessionsForModification;

import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.EmailException;

/**
 * @author <a href="mailto:aaa@bbb.ccc">xXx xxXXxx</a>
 */
public class AutoOTPEndpoint {

    private static final Logger logger = Logger.getLogger(AutoOTPEndpoint.class);
    private MultivaluedMap<String, String> formParams;
    private ClientModel client;
    private Map<String, String> clientAuthAttributes;

    private final KeycloakSession session;

    private final HttpRequest request;

    private final org.keycloak.http.HttpResponse httpResponse;

    private final HttpHeaders headers;

    private final ClientConnection clientConnection;

    private final RealmModel realm;
    private final EventBuilder event;

    private Cors cors;

    public AutoOTPEndpoint(KeycloakSession session, EventBuilder event) {
        this.session = session;
        this.clientConnection = session.getContext().getConnection();
        this.realm = session.getContext().getRealm();
        this.event = event;
        this.request = session.getContext().getHttpRequest();
        this.httpResponse = session.getContext().getHttpResponse();
        this.headers = session.getContext().getRequestHeaders();
    }

    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @POST
    public Response processGrantRequest() {
        // grant request needs to be run in a retriable transaction as concurrent execution of this action can lead to
        // exceptions on DBs with SERIALIZABLE isolation level.
        return KeycloakModelUtils.runJobInRetriableTransaction(session.getKeycloakSessionFactory(), new ResponseSessionTask(session) {
            @Override
            public Response runInternal(KeycloakSession session) {
                // create another instance of the endpoint to isolate each run.
            	AutoOTPEndpoint other = new AutoOTPEndpoint(session, new EventBuilder(session.getContext().getRealm(), session, clientConnection));
                // process the request in the created instance.
                return other.processGrantRequestInternal();
            }
        }, 10, 100);
    }

    private Response processGrantRequestInternal() {
        cors = Cors.add(request).auth().allowedMethods("POST").auth().exposedHeaders(Cors.ACCESS_CONTROL_ALLOW_METHODS);

        MultivaluedMap<String, String> formParameters = request.getDecodedFormParameters();

        if (formParameters == null) {
            formParameters = new MultivaluedHashMap<>();
        }

        formParams = formParameters;

        // https://tools.ietf.org/html/rfc6749#section-5.1
        // The authorization server MUST include the HTTP "Cache-Control" response header field
        // with a value of "no-store" as well as the "Pragma" response header field with a value of "no-cache".
        httpResponse.setHeader("Cache-Control", "no-store");
        httpResponse.setHeader("Pragma", "no-cache");

        checkSsl();
        checkRealm();

        // Realm 사용여부체크
        boolean isRealmEnabled = realm.isEnabled();

        String msg = "OK";
        String code = "000";
        
    	String url = "";
    	String params = "";
    	String ip = "";

    	List<String> listUrl = (List<String>) formParams.get("url");
    	List<String> listParams = (List<String>) formParams.get("params");
    	
    	String[] arrUrl = listUrl.toArray(new String[listUrl.size()]);
    	String[] arrParams = listParams.toArray(new String[listParams.size()]);
    	
    	url = arrUrl[0];
    	params = arrParams[0];

    	System.out.println("############################### AutoOTPEndpoint :: processGrantRequestInternal - isRealmEnabled [" + isRealmEnabled + "] formParams [" + formParams.toString() + "] url [" + url + "] params [" + params + "]");

    	Map<String, Object> callResult = null;

        // DB정보조회
        String dbDomain = realm.getAttribute("autootpAppSettingDomain");
        String dbEmail = realm.getAttribute("autootpAppSettingEmail");
        String dbIpAddr = realm.getAttribute("autootpAppSettingIpAddress");
        String dbName = realm.getAttribute("autootpAppSettingName");
        String dbProxyDomain = realm.getAttribute("autootpAppSettingProxyServerDomain");
        String dbStep = realm.getAttribute("autootpAppSettingStep");
        String dbDomainValidToken = realm.getAttribute("autootpReturnDomainValidationToken");
        String dbSecretKey = realm.getAttribute("autootpServerSettingAppServerKey");
        String dbAuthDomain = realm.getAttribute("autootpServerSettingAuthServerDomain");
        
        if(url.equals("isApUrl") || url.equals("sendEmail")) {
	    	System.out.println("autootpAppSettingDomain [" + dbDomain + "]");
	    	System.out.println("autootpAppSettingEmail [" + dbEmail + "]");
	    	System.out.println("autootpAppSettingIpAddress [" + dbIpAddr + "]");
	    	System.out.println("autootpAppSettingName [" + dbName + "]");
	    	System.out.println("autootpAppSettingProxyServerDomain [" + dbProxyDomain + "]");
	    	System.out.println("autootpAppSettingStep [" + dbStep + "]");
	    	System.out.println("autootpReturnDomainValidationToken [" + dbDomainValidToken + "]");
	    	System.out.println("autootpServerSettingAppServerKey [" + dbSecretKey + "]");
	    	System.out.println("autootpServerSettingAuthServerDomain [" + dbAuthDomain + "]");
        }
        

    	if(url.equals("sendEmail")) {
    		System.out.println(">>>>>>>>>>>>>>> send email : session=" + session.toString() + ", realm=" + realm.toString());

    		String targetUser = "";
    		String baseUrl = "";
    		
    		Map<String, String> mapParams = getParamsKeyValue(params);
 			Set<String> set = mapParams.keySet();
 			Iterator<String> keyset = set.iterator();
 			while(keyset.hasNext()) {
 				String key = keyset.next();
 				String value = mapParams.get(key);
 				
 				if(key.equals("userId"))
 					targetUser = value;
 				
 				if(key.equals("base_url"))
 					baseUrl = value;
 			}

 			/*
    		UserSessionModel userSession = null;
    		AuthenticationManager.AuthResult authResult = AuthenticationManager.authenticateIdentityCookie(session, realm, false);
    		if (authResult != null) {
				userSession = authResult.getSession();
				UserModel user = userSession.getUser();
				
				//String username = userSession.getLoginUsername();
				//System.out.println(">>>>>>>>>>>>>>> send email : username [" + username + "]");
				
		        boolean isEnabled = user.isEnabled();
		        boolean isEmailVerified = user.isEmailVerified();
		        String strIsEnabled = "F";
		        String strIsEmailVerified = "F";
		        if(isEnabled)		strIsEnabled = "T";
		        if(isEmailVerified)	strIsEmailVerified = "T";
		        
		        String userId = user.getId();
		        String username = user.getUsername();
		        String firstName = user.getFirstName();
		        String lastName = user.getLastName();
		        String email = user.getEmail();
		        
		        System.out.println(">>>>>>>>>>>>>>> send email : strIsEnabled [" + strIsEnabled + "] strIsEmailVerified [" + strIsEmailVerified + "] userId [" + userId + "] username [" + username + "] lastName [" + lastName + "] firstName [" + firstName + "] email [" + email + "]");
    		}
    		else {
    			System.out.println(">>>>>>>>>>>>>>> send email : Not login state !!!");
    		}
    		*/
 			
    		/*
    		String id = "18380ca3-76d6-4d9a-8ea0-062e8e8ddd71"; // tunar1
    		org.keycloak.admin.client.resource.UserResource userInfo = realm.users().get(id);

	        try {
	            userInfo.sendVerifyEmail();
	            System.out.println(">>>>>>>>>>>>>>> send email : OK");
	        } catch (ClientErrorException e) {
	        	System.out.println(">>>>>>>>>>>>>>> send email : Failed --> " + e.toString());
	        }
			*/
    		
 			UserModel user = session.users().getUserByUsername(realm, targetUser);
 			System.out.println(">>>>>>>>>>>>> targetUser=" + targetUser + ", user=" + user + ", email=" + user.getEmail() + ", baseUrl=" + baseUrl);
 			
    		String addr = user.getEmail();
    		String link = "realms/" + realm.getName() + "/login-actions/autootp-regist";
    		//long expirationInMinutes = realm.getAccessCodeLifespanUserAction() / 60;
    		long expirationInMinutes = realm.getActionTokenGeneratedByUserLifespan() / 60;
    		
    		try {
    			System.out.println(">>>>>>>>>>>>> sendAutoOTPEmail start....");
    			
				UserModel userModel = null; //session.users().addUser(session.getContext().getRealm(), "tunar1");
				//System.out.println(">>>>>>>>>>>>> sendAutoOTPEmail user=" + userModel.toString());
				
    			session.getProvider(EmailTemplateProvider.class).sendAutoOTPEmail(link, targetUser, dbSecretKey, dbAuthDomain, expirationInMinutes, addr, baseUrl);
    			System.out.println(">>>>>>>>>>>>> sendAutoOTPEmail send : link [" + link + "] target [" + targetUser + "] secretKey [" + dbSecretKey + "] authDoamin [" + dbAuthDomain + "] expMin [" + expirationInMinutes + "] addr [" + addr + "] baseUrl [" + baseUrl + "]");
    		} catch (EmailException ee) {
    			System.out.println(">>>>>>>>>>>>> sendAutoOTPEmail EmailException error : " + ee.toString());

    			msg = ee.toString();
    			code = "998";
    		} catch (Exception e) {
    			System.out.println(">>>>>>>>>>>>> sendAutoOTPEmail Exception error : " + e.toString());
    			
    			msg = e.toString();
    			code = "999";
    		}
	        
    		
    		Map<String, String> result = new HashMap<String, String>();
    		result.put("msg", msg);
    		result.put("code", code);
    		
    		callResult = new HashMap<String, Object>();
    		callResult.put("result", result);
    	}
    	else {
	        String secretKey = dbSecretKey;
	    	//secretKey = "7af7c8d6568e28e9";
	    	
	    	// AutoOTP 인증서버 URL
	    	String auth_url = dbAuthDomain;
	    	//auth_url = "http://twowin-auth.autootp.com:11040";
	    	
	    	System.out.println("############################### AutoOTPEndpoint :: processGrantRequestInternal - ip [" + ip + "] secretKey [" + secretKey + "] auth_url [" + auth_url + "]");
	    	
	    	checkParameterDuplicated();
	    	
	    	//return resourceOwnerPasswordCredentialsGrant();
	    	
	    	callResult = callAutoOTPReq(secretKey, auth_url, url, params);
    	}
    	
        //String str = "{\"key\":\"res.string\", \"value\":\"AutoOTP response text\"}";
        cors.build(httpResponse);
        return cors.builder(Response.ok(callResult, MediaType.APPLICATION_JSON_TYPE)).build();

        //throw new RuntimeException("Unknown action " + action);
    }

    @OPTIONS
    public Response preflight() {
        if (logger.isDebugEnabled()) {
            logger.debugv("CORS preflight from: {0}", headers.getRequestHeaders().getFirst("Origin"));
        }
        return Cors.add(request, Response.ok()).auth().preflight().allowedMethods("POST", "OPTIONS").build();
    }

    private void checkSsl() {
        if (!session.getContext().getUri().getBaseUri().getScheme().equals("https") && realm.getSslRequired().isRequired(clientConnection)) {
            throw new CorsErrorResponseException(cors.allowAllOrigins(), OAuthErrorException.INVALID_REQUEST, "HTTPS required", Response.Status.FORBIDDEN);
        }
    }

    private void checkRealm() {
        if (!realm.isEnabled()) {
            throw new CorsErrorResponseException(cors.allowAllOrigins(), "access_denied", "Realm not enabled", Response.Status.FORBIDDEN);
        }
    }

    private void checkParameterDuplicated() {
        for (String key : formParams.keySet()) {
            if (formParams.get(key).size() != 1) {
                throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, "duplicated parameter",
                    Response.Status.BAD_REQUEST);
            }
        }
    }
    
    // ----------------------------------------------------------------------------------------- 내부함수
    
    public Map<String, Object> callAutoOTPReq(String db_secretKey, String auth_url, String url, String params) {
    	
    	// 변경 시 1회만 노출됨
    	//String secretKey = "6df2d83a754a12ba";
    	//String secretKey = "7af7c8d6568e28e9";

    	// AutoOTP 등록여부 확인
    	String isApUrl = auth_url + "/ap/rest/auth/isAp";

    	// AutoOTP 등록 REST API
    	String joinApUrl = auth_url + "/ap/rest/auth/joinAp";

    	// AutoOTP 해지 REST API
    	String withdrawalApUrl = auth_url + "/ap/rest/auth/withdrawalAp";

    	// AutoOTP 일회용토큰 요청 REST API
    	String getTokenForOneTimeUrl = auth_url + "/ap/rest/auth/getTokenForOneTime";

    	// AutoOTP 인증요청 REST API
    	String getSpUrl = auth_url + "/ap/rest/auth/getSp";

    	// AutoOTP 인증 결과 요청 REST API
    	String resultUrl = auth_url + "/ap/rest/auth/result";

    	// AutoOTP 인증요청 취소 REST API
    	String cancelUrl = auth_url + "/ap/rest/auth/cancel";

    	String random = System.currentTimeMillis() + "";
    	String sessionId = System.currentTimeMillis() + "_sessionId";
    	String apiUrl = "";
    	String ip = "";
    	
    	if(url.equals("isApUrl"))				{ apiUrl = isApUrl; }
		if(url.equals("joinApUrl"))				{ apiUrl = joinApUrl; }
		if(url.equals("withdrawalApUrl"))		{ apiUrl = withdrawalApUrl; }
		if(url.equals("getTokenForOneTimeUrl"))	{ apiUrl = getTokenForOneTimeUrl; }
		if(url.equals("getSpUrl"))				{ apiUrl = getSpUrl; params += "&clientIp=" + ip + "&sessionId=" + sessionId + "&random=" + random + "&password="; }
		if(url.equals("resultUrl"))				{ apiUrl = resultUrl;}
		if(url.equals("cancelUrl"))				{ apiUrl = cancelUrl;}
		
		System.out.println("(AutoOTP 요청) autoOTPReq: url [" + url + "], param [" + params + "], apiUrl [" + apiUrl + "] secretKey [" + db_secretKey + "]");
		
		String result = "";
		
		if(!apiUrl.equals("")) {
			result = callApi("POST", apiUrl, params);
		}
		System.out.println("result [" + result + "]");

		Map<String, Object> mapResult = new HashMap<String, Object>();
		
		// 1회용 토큰 요청
		if(url.equals("getTokenForOneTimeUrl")) {
			String oneTimeToken = "";
			JsonElement element = JsonParser.parseString(result);
			JsonObject data = element.getAsJsonObject().get("data").getAsJsonObject();
			String token = data.getAsJsonObject().get("token").getAsString();
			oneTimeToken = getDecryptAES(token, db_secretKey.getBytes());
			
			System.out.println("token [" + token + "] --> oneTimeToken [" + oneTimeToken + "]");
			
			mapResult.put("oneTimeToken", oneTimeToken);
		}
		
		// AutoOTP 인증요청 REST API
		if(url.equals("getSpUrl")) {
			mapResult.put("sessionId", sessionId);
		}
		
		// AutoOTP 승인 대기
		String dateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String myInfo = "";
		if(url.equals("resultUrl")) {
			JsonElement element = JsonParser.parseString(result);
			JsonObject data = element.getAsJsonObject().get("data").getAsJsonObject();
			String auth = data.getAsJsonObject().get("auth").getAsString();
			String userId = data.getAsJsonObject().get("userId").getAsString();
			
			if(auth.equals("Y")) {
				// 로그인 성공 데이터
				myInfo = getEncryptAES(dateTime + "|||" + userId, db_secretKey.getBytes());
				System.out.println("AutoOTP accept result 'Y' --> myInfo [" + dateTime + "|||" + userId + "]");
			}
		}
		
		mapResult.put("autootpInfo", myInfo);
		mapResult.put("result", result);
		
		return mapResult;
    }

 	public Map<String, String> getParamsKeyValue(String params) {
 		String[] arrParams = params.split("&");
         Map<String, String> map = new HashMap<String, String>();
         for (String param : arrParams)
         {
         	String name = "";
         	String value = "";
         	
         	String[] tmpArr = param.split("=");
             name = tmpArr[0];
             
             if(tmpArr.length == 2)
             	value = tmpArr[1];
             
             map.put(name, value);
         }

         return map;
 	}
 	
 	public String callApi(String type, String requestURL, String params) {

 		String retVal = "";
 		Map<String, String> mapParams = getParamsKeyValue(params);

 		try {
 			URIBuilder b = new URIBuilder(requestURL);
 			
 			Set<String> set = mapParams.keySet();
 			Iterator<String> keyset = set.iterator();
 			while(keyset.hasNext()) {
 				String key = keyset.next();
 				String value = mapParams.get(key);
 				b.addParameter(key, value);
 			}
 			URI uri = b.build();
 	
 	        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
 	        
 	       org.apache.http.HttpResponse response;
 	        
 	        if(type.toUpperCase().equals("POST")) {
 		        HttpPost httpPost = new HttpPost(uri);
 		        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
 	        	response = httpClient.execute(httpPost);
 	        }
 	        else {
 	        	HttpGet httpGet = new HttpGet(uri);
 	        	httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
 	        	response = httpClient.execute(httpGet);
 	        }
 	        
 	        HttpEntity entity = response.getEntity();
 	        retVal = EntityUtils.toString(entity);
 		} catch(Exception e) {
 			System.out.println(e.toString());
 		}
 		
 		return retVal;
 	}
 	
 	private static String getEncryptAES(String value, byte[] key) {
 		String strRet = null;
 		
 		byte[]  strIV = key;
 		if ( key == null || strIV == null ) return null;
	    try {
	        SecretKey secureKey = new SecretKeySpec(key, "AES");
 			Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
 			c.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(strIV));
	        byte[] byteStr = c.doFinal(value.getBytes());
	        strRet = java.util.Base64.getEncoder().encodeToString(byteStr);
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    return strRet;
	}
 	
 	private static String getDecryptAES(String encrypted, byte[] key) {
 		String strRet = null;
 		
 		byte[]  strIV = key;
 		if ( key == null || strIV == null ) return null;
 		try {
 			SecretKey secureKey = new SecretKeySpec(key, "AES");
 			Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
 			c.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(strIV));
 			byte[] byteStr = java.util.Base64.getDecoder().decode(encrypted);//Base64Util.getDecData(encrypted);
 			strRet = new String(c.doFinal(byteStr), "utf-8");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return strRet;	
 	}
 	
 	private static String getHash(String data, String secretKey) { 
 		String HMAC_SHA1_ALGORITHM = "HmacSHA1";
 		try {
 			SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(), HMAC_SHA1_ALGORITHM);
 		    Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);	
 		    mac.init(signingKey);
 		    return Hex.encodeHexString(mac.doFinal(data.getBytes()));
 		} catch (Exception e) {
 			return null;
 		}
 	}
}