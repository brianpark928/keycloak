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
import javax.ws.rs.GET;
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

import static org.keycloak.utils.LockObjectsForModification.lockUserSessionsForModification;



import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;




import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//import javax.crypto.Cipher;
//import java.io.UnsupportedEncodingException;
import java.security.*;
//import java.security.spec.PKCS8EncodedKeySpec;
//import java.security.spec.X509EncodedKeySpec;
//import java.util.Base64;
//import java.util.HashMap;





/**
 * @author <a href="mailto:aaa@bbb.ccc">xXx xxXXxx</a>
 */
public class AutootpPolicyEndpoint {

    private static final Logger logger = Logger.getLogger(AutootpPolicyEndpoint.class);
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
    
    private static String mpublicKey = "";
    private static String mprivateKey = "";


    public AutootpPolicyEndpoint(KeycloakSession session, EventBuilder event) {
    	
    	
        this.session = session;
        this.clientConnection = session.getContext().getConnection();
        this.realm = session.getContext().getRealm();
        this.event = event;
        this.request = session.getContext().getHttpRequest();
        this.httpResponse = session.getContext().getHttpResponse();
        this.headers = session.getContext().getRequestHeaders();
        this.mpublicKey = realm.getAttribute("autootpAppSettingPublickey");
        this.mprivateKey = realm.getAttribute("autootpAppSettingPrivatekey");
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @GET
    public Response processGrantRequest() {
    	
        // grant request needs to be run in a retriable transaction as concurrent execution of this action can lead to
        // exceptions on DBs with SERIALIZABLE isolation level.
        return KeycloakModelUtils.runJobInRetriableTransaction(session.getKeycloakSessionFactory(), new ResponseSessionTask(session) {
            @Override
            public Response runInternal(KeycloakSession session) {
                // create another instance of the endpoint to isolate each run.
            	AutootpPolicyEndpoint other = new AutootpPolicyEndpoint(session, new EventBuilder(session.getContext().getRealm(), session, clientConnection));
                // process the request in the created instance.
                return other.processGrantRequestInternal();
            }
        }, 10, 100);
    }

    private Response processGrantRequestInternal() {
        cors = Cors.add(request).auth().allowedMethods("POST").auth().exposedHeaders(Cors.ACCESS_CONTROL_ALLOW_METHODS);

        MultivaluedMap<String, String> formParameters = session.getContext().getUri().getQueryParameters();
    	System.out.println("############################### AutootpPolicyEndpoint :: processGrantRequestInternal - formParameters [" + formParameters.toString() + "] ");

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
    	
    	String appID = "";
    	String urlKey = "";
    	String url = "";
    	String param1 = "";
    	String param2 = "";
    	String params = "";
    	String ip = "";
    	String secretKey = "";
    	String privateKey = "";
    	String methodType = "GET";
//    	String developerUrl = "https://testdevelopers.autootp.com";
    	String developerUrl = "https://developers.autootp.com";
    	
        List<String> values = null;
    	int i = 0;

        String KeyValueStr = "";
        if (formParameters != null) {
          for (String key : formParameters.keySet()) {
        	values = formParameters.get(key);
        	if(key.equals("urlKey")) {
                urlKey = values.toString();
        	}else if(key.equals("appID")) {
                appID = values.toString().replaceAll("\\[","").replaceAll("\\]","");
	      	}else {
	        	if(i > 0) {	KeyValueStr = KeyValueStr + "&"; }
	        	if(key.equals("delkey")) {
	        		try {
		                String signText = sign(values.toString().replaceAll("\\[","").replaceAll("\\]",""),mprivateKey);		// 개인키로 delkey를 전자인증 sign값으로 전송 
		                KeyValueStr = KeyValueStr + "sign=" +  URLEncode(signText); 											// 전자인증 sign값을 URLEncode 처리 
	        		}catch (Exception e) {
	        			System.out.println("############################### kcAutootpAppSave :: RSA Sign 오류 [" + e.toString() + "] ");
	        		}
	        	}else {
		            KeyValueStr = KeyValueStr + key + "=" +  URLEncode(values.toString().replaceAll("\\[","").replaceAll("\\]",""));
	        	}
	    	}
    		i++;
          }
          
          switch(urlKey) {
          case "[kcAutootpAppSave]": 
        	  url = developerUrl + "/aod/rest/keyclock";
        	  methodType = "POST";
        	  try {
            	  
                  if(realm.getAttribute("autootpAppSettingPublickey") == null || realm.getAttribute("autootpAppSettingPublickey").equals("")) {
                      createRsaGenKey(); // RSA PublicKey/PrivateKey create
                	  realm.setAttribute("autootpAppSettingPublickey", mpublicKey);
                	  realm.setAttribute("autootpAppSettingPrivatekey", mprivateKey);
                  }
            	  KeyValueStr = KeyValueStr + "&pubKey=" +  URLEncode(realm.getAttribute("autootpAppSettingPublickey"));
        		  
        	  }catch (Exception e) {
        		  System.out.println("############################### kcAutootpAppSave ::RSA PublicKey/PrivateKey create Error [" + e.toString() + "] ");
        	  }
        	  break;
          case "[kcAutootpDeleteKey]": 
        	  url = developerUrl + "/aod/rest/keyclock/"+appID+"/delkey";
        	  methodType = "GET";
        	  break;
          case "[kcAutootpDelete]": 
        	  url = developerUrl + "/aod/rest/keyclock/"+appID+"/del";
        	  methodType = "GET";
        	  break;
          case "[kcDevcenterReload]": 
        	  url = developerUrl + "/aod/rest/keyclock/"+appID;
        	  methodType = "GET";
        	  break;
          case "[kcDevcenterRemail]": 
        	  url = developerUrl + "/aod/rest/keyclock/mail/"+appID;
        	  methodType = "GET";
        	  break;
          case "[kcDevcenterRemailSetting]": 
        	  url = developerUrl + "/aod/rest/keyclock/mail/setting/"+appID;
        	  methodType = "GET";
        	  break;
          }
          System.out.println("############################### AutootpPolicyEndpoint :: processGrantRequestInternal - urlKey [" + urlKey + "] ");
          System.out.println("############################### AutootpPolicyEndpoint :: processGrantRequestInternal - url [" + url + "] ");
          System.out.println("############################### AutootpPolicyEndpoint :: processGrantRequestInternal - appID [" + appID + "] ");
          System.out.println("############################### AutootpPolicyEndpoint :: processGrantRequestInternal - KeyValueStr [" + KeyValueStr + "] ");
        }
    	
    	Map<String, Object> callResult = callServerApi(methodType, url, KeyValueStr);
    	
        cors.build(httpResponse);
        return cors.builder(Response.ok(callResult, MediaType.APPLICATION_JSON_TYPE)).build();
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
    
    public Map<String, Object> callServerApi(String methodType, String url, String params) {
    	
		String result = "";
		
		//result = callApi( methodType, url, params);
		
 		try {
 			if(methodType.equals("POST")) {	
 				result = sendPost(url, params);	
 	 			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ sendPost result [" + result + "]");
 			} 
 			else {	
 				result = sendGet(url, params);	
 	 			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ sendGet result [" + result + "]");
 			}
 		} catch(Exception e) {
 			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ callServerApi Error: " + e);
 		}

		System.out.println("result [" + result + "]");

		Map<String, Object> mapResult = new HashMap<String, Object>();
		

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
 		System.out.println("params [" + params + "]");
 		System.out.println("type [" + type + "]");
 		System.out.println("requestURL [" + requestURL + "]");
 		System.out.println("map [" + mapParams.toString() + "]");

 		try {
 			URIBuilder b = new URIBuilder(requestURL);
 			
 			Set<String> set = mapParams.keySet();
 			Iterator<String> keyset = set.iterator();
 			while(keyset.hasNext()) {
 				String key = keyset.next();
 				String value = URLEncode(mapParams.get(key));
 				//String value = mapParams.get(key);
 				b.addParameter(key, value);
 				
 				System.out.println("key [" + key + "] value [" + value + "]");
 			}
 			URI uri = b.build();
 	
 	        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
 	        
 	       org.apache.http.HttpResponse response;
 	        
 	        if(type.toUpperCase().equals("POST")) {
 		        HttpPost httpPost = new HttpPost(uri);
 		        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");;
 	        	response = httpClient.execute(httpPost);
 	        	
 	        	System.out.println("response [" + response.toString() + "]");
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
 	
 	
 	
 	
    /**
     * "KeycloalAutoOTP" 를 이용해 random 값을 추출하여 2048bit에 해당하는 키 생성
     * @throws Exception
     */
    private static void createRsaGenKey() throws Exception{

        String pubkey = "KeycloalAutoOTP";

        SecureRandom random = new SecureRandom(pubkey.getBytes());
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA","SunJSSE"); // OK
        generator.initialize(2048, random); // 여기에서는 2048 bit 키를 생성하였음

        KeyPair pair = generator.generateKeyPair();
        Key pubKey = pair.getPublic(); // Kb(pub) 공개키
        Key privKey = pair.getPrivate();// Kb(pri) 개인키
        
        mpublicKey = Base64.getEncoder().encodeToString(pubKey.getEncoded());
        mprivateKey = Base64.getEncoder().encodeToString(privKey.getEncoded());
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% createRsaGenKey :: mpublicKey [" + mpublicKey + "] ");    
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% createRsaGenKey :: mprivateKey [" + mprivateKey + "] ");    
        
    }

    
    /**
     * 개인키로 암호화
     * @param encStr
     * @return byteArrayToHex(cipherText)
     * @throws Exception
     */
/*    
    private static String rsaPrivateEnc(String encStr) throws Exception{

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING", "SunJCE"); // 알고리즘 명 / Cipher 알고리즘 mode / padding

        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% rsaPrivateEnc :: mprivateKey 저장되어있는 개인키값 [" + mprivateKey + "] ");    
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% rsaPrivateEnc :: 암호화 전 delkey [" + encStr + "] ");    
        PKCS8EncodedKeySpec rkeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(mprivateKey));

        KeyFactory ukeyFactory = KeyFactory.getInstance("RSA");

        PrivateKey privatekey = null;

        try {
            // privatekey에 공용키 값 설정
        	privatekey = ukeyFactory.generatePrivate(rkeySpec);

        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] input = encStr.getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, privatekey);

        byte[] cipherText = cipher.doFinal(input);

        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% rsaPrivateEnc :: 암호화 후 delkey [" + byteArrayToHex(cipherText)+ "] ");    
        return byteArrayToHex(cipherText);

    }
*/
    
    /**
     * 공용키로 복호화
     * @param byteArrayToHex(cipherText) ==> decStr
     * @return
     * @throws Exception
     */
/*    
    private static String rsaPublicDec(String decStr) throws Exception{

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING", "SunJCE");

        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% rsaPublicDec :: mpublicKey [" + mpublicKey + "] ");    
        X509EncodedKeySpec ukeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(mpublicKey));
        KeyFactory rkeyFactory = KeyFactory.getInstance("RSA");

        PublicKey publicKey = null;

        try {
        	publicKey = rkeyFactory.generatePublic(ukeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 복호
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] plainText = cipher.doFinal(hexToByteArray(decStr));

        String returnStr = new String(plainText);

        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% rsaPublicDec :: 복호화 후 delkey [" + returnStr+ "] ");    
        return returnStr;
    }
*/    
    
    /**
     * 암호화
     * @param encStr
     * @return byteArrayToHex(cipherText)
     * @throws Exception
     */
/*    
    private static String rsaEnc(String encStr) throws Exception{

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING", "SunJCE"); // 알고리즘 명 / Cipher 알고리즘 mode / padding

        X509EncodedKeySpec ukeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(mpublicKey));

        KeyFactory ukeyFactory = KeyFactory.getInstance("RSA");

        PublicKey publickey = null;

        try {
            // PublicKey에 공용키 값 설정
            publickey = ukeyFactory.generatePublic(ukeySpec);

        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] input = encStr.getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, publickey);

        byte[] cipherText = cipher.doFinal(input);

        return byteArrayToHex(cipherText);

    }
*/
    /**
     * 복호화
     * @param byteArrayToHex(cipherText) ==> decStr
     * @return
     * @throws Exception
     */
/*    
    private static String rsaDec(String decStr) throws Exception{

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING", "SunJCE");

        PKCS8EncodedKeySpec rkeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(mprivateKey));
        KeyFactory rkeyFactory = KeyFactory.getInstance("RSA");

        PrivateKey privateKey = null;

        try {
            privateKey = rkeyFactory.generatePrivate(rkeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 복호
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] plainText = cipher.doFinal(hexToByteArray(decStr));

        String returnStr = new String(plainText);

        return returnStr;
    }
*/



    // hex string to byte[]
/*    
    public static byte[] hexToByteArray(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }
        byte[] ba = new byte[hex.length() / 2];
        for (int i = 0; i < ba.length; i++) {
            ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return ba;
    }
*/
    // byte[] to hex sting
/*    
    public static String byteArrayToHex(byte[] ba) {
        if (ba == null || ba.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer(ba.length * 2);
        String hexNumber;
        for (int x = 0; x < ba.length; x++) {
            hexNumber = "0" + Integer.toHexString(0xff & ba[x]);

            sb.append(hexNumber.substring(hexNumber.length() - 2));
        }
        return sb.toString();
    }
*/
    
    // URLEncoder
    public String URLEncode(String param) {
    	String retVal = "";
    	
    	try {
            retVal = URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
    	
    	return retVal;
    }
 	

    /**
     * 암호화
     */
/*    
    static String encode(String plainData, String stringPublicKey) {
        String encryptedData = null;
        try {
            //평문으로 전달받은 공개키를 공개키객체로 만드는 과정
            PublicKey publicKey =  getPublicKey(stringPublicKey);
            //만들어진 공개키객체를 기반으로 암호화모드로 설정하는 과정
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            //평문을 암호화하는 과정
            byte[] byteEncryptedData = cipher.doFinal(plainData.getBytes());
            encryptedData = Base64.getEncoder().encodeToString(byteEncryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedData;
    }
*/    
    /**
     * 복호화
     */
/*
    static String decode(String encryptedData, String stringPrivateKey) {
        String decryptedData = null;
        try {
            //평문으로 전달받은 개인키를 개인키객체로 만드는 과정
            PrivateKey privateKey = getPrivateKey(stringPrivateKey);
            //만들어진 개인키객체를 기반으로 암호화모드로 설정하는 과정
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            //암호문을 평문화하는 과정
            byte[] byteEncryptedData = Base64.getDecoder().decode(encryptedData.getBytes());
            byte[] byteDecryptedData = cipher.doFinal(byteEncryptedData);
            decryptedData = new String(byteDecryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedData;
    }
*/	
    static PublicKey getPublicKey(String stringPublicKey) {
        PublicKey publicKey = null;
        try {
            //평문으로 전달받은 공개키를 공개키객체로 만드는 과정
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] bytePublicKey = Base64.getDecoder().decode(stringPublicKey.getBytes());
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bytePublicKey);
            publicKey = keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return publicKey;
    }
    static PrivateKey getPrivateKey(String stringPrivateKey) {
        PrivateKey privateKey = null;
        try {
            //평문으로 전달받은 개인키를 개인키객체로 만드는 과정
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] bytePrivateKey = Base64.getDecoder().decode(stringPrivateKey.getBytes());
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bytePrivateKey);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return privateKey;
    }
    public static String sign(String plainText,String strPrivateKey) {
        try {
        	System.out.println("전자서명 전 plainText값====================="+plainText); 
        	System.out.println("전자서명 전 개인키값====================="+strPrivateKey); 
        	PrivateKey privateKey = getPrivateKey(strPrivateKey);
        	Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(privateKey);
            privateSignature.update(plainText.getBytes("UTF-8"));
            byte[] signature = privateSignature.sign();
        	System.out.println("전자서명 후 sign값====================="+Base64.getEncoder().encodeToString(signature)); 
            return Base64.getEncoder().encodeToString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean verifySignarue(String plainText, String signature, String strPublicKey) {
        Signature sig;
        try {
            PublicKey publicKey = getPublicKey(strPublicKey);
            sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(plainText.getBytes());
            if (!sig.verify(Base64.getDecoder().decode(signature)));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
	
	
	
	
	// --------------------------------------------------
 	
 	
 	public String sendPost(String url, String urlParameters) throws Exception {
 		
 		String USER_AGENT = "Mozilla/5.0";
 		
		URL obj = new URL(url);
		ignoreSsl();
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ sendPost ("+url+") :: ["+responseCode+"]"+ response.toString());
		return response.toString();

	}
 	
	public String sendGet(String url, String urlParameters) throws Exception {

 		String USER_AGENT = "Mozilla/5.0";
 		String urlStr = url;
 		if(urlParameters != null && !urlParameters.equals("")) {
 			urlStr = url + "?" + urlParameters;
 		}
 		
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ sendGet openConnection ("+urlStr+") ");
		URL obj = new URL(urlStr);
		ignoreSsl();
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ sendGet ("+urlStr+") :: ["+responseCode+"]"+ response.toString());
		return response.toString();
	}
 	
 	public static void ignoreSsl() throws Exception{
        HostnameVerifier hv = new HostnameVerifier() {
        	public boolean verify(String urlHostName, SSLSession session) { 
                return true;
            }
        };
        trustAllHttpsCertificates();
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }
 	
 	private static void trustAllHttpsCertificates() throws Exception {
 	    TrustManager[] trustAllCerts = new TrustManager[1];
 	    TrustManager tm = new miTM();
 	    trustAllCerts[0] = tm;
 	    SSLContext sc = SSLContext.getInstance("SSL");
 	    sc.init(null, trustAllCerts, null);
 	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 	}

 	static class miTM implements TrustManager,X509TrustManager {
 	   
 		@Override
 		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
 			
 		}

 		@Override
 		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
 			
 		}

 		@Override
 		public X509Certificate[] getAcceptedIssuers() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 	}
	
	
}
