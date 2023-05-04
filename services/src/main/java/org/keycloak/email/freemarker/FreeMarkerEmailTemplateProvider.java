/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.email.freemarker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import java.net.URLEncoder;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.beans.EventBean;
import org.keycloak.email.freemarker.beans.ProfileBean;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.freemarker.model.UrlBean;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.Theme;
import org.keycloak.theme.beans.LinkExpirationFormatterMethod;
import org.keycloak.theme.beans.MessageFormatterMethod;
import org.keycloak.theme.freemarker.FreeMarkerProvider;
import org.keycloak.utils.StringUtil;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class FreeMarkerEmailTemplateProvider implements EmailTemplateProvider {

    protected KeycloakSession session;
    /**
     * authenticationSession can be null for some email sendings, it is filled only for email sendings performed as part of the authentication session (email verification, password reset, broker link
     * etc.)!
     */
    protected AuthenticationSessionModel authenticationSession;
    protected FreeMarkerProvider freeMarker;
    protected RealmModel realm;
    protected UserModel user;
    protected final Map<String, Object> attributes = new HashMap<>();

    public FreeMarkerEmailTemplateProvider(KeycloakSession session) {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: FreeMarkerEmailTemplateProvider");
        this.session = session;
        this.freeMarker = session.getProvider(FreeMarkerProvider.class);
    }

    @Override
    public EmailTemplateProvider setRealm(RealmModel realm) {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: setRealm(" + realm + ")");
        this.realm = realm;
        return this;
    }

    @Override
    public EmailTemplateProvider setUser(UserModel user, ClientModel client) {
    	String baseUrl = client.getBaseUrl();
    	attributes.put("baseUrl", baseUrl);
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: baseUrl [" + baseUrl + "]");

    	return setUser(user);
    }
    
    @Override
    public EmailTemplateProvider setUser(UserModel user) {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: setUser(" + user + ")");
        this.user = user;
        
        
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
        
        
        
        // Realm, User 정보
        System.out.println(".......................................................");
        
        AuthenticationFlowModel flowModel = realm.getBrowserFlow();
        String dbBrowserFlowAlias = flowModel.getAlias();
        
        if(dbBrowserFlowAlias == null)
        	dbBrowserFlowAlias = "";
        
        if(dbBrowserFlowAlias.toUpperCase().indexOf("AUTOOTP") > -1)
        	dbBrowserFlowAlias = "AUTOOTP";
    	
    	String dateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        
        String dbDomain = realm.getAttribute("autootpAppSettingDomain");
        String dbEmail = realm.getAttribute("autootpAppSettingEmail");
        String dbIpAddr = realm.getAttribute("autootpAppSettingIpAddress");
        String dbName = realm.getAttribute("autootpAppSettingName");
        String dbProxyDomain = realm.getAttribute("autootpAppSettingProxyServerDomain");
        String dbStep = realm.getAttribute("autootpAppSettingStep");
        String dbDomainValidToken = realm.getAttribute("autootpReturnDomainValidationToken");
        String dbSecretKey = realm.getAttribute("autootpServerSettingAppServerKey");
        String dbAuthDomain = realm.getAttribute("autootpServerSettingAuthServerDomain");
        
    	System.out.println("dbBrowserFlowAlias [" + dbBrowserFlowAlias + "]");
    	System.out.println("autootpAppSettingDomain [" + dbDomain + "]");
    	System.out.println("autootpAppSettingEmail [" + dbEmail + "]");
    	System.out.println("autootpAppSettingIpAddress [" + dbIpAddr + "]");
    	System.out.println("autootpAppSettingName [" + dbName + "]");
    	System.out.println("autootpAppSettingProxyServerDomain [" + dbProxyDomain + "]");
    	System.out.println("autootpAppSettingStep [" + dbStep + "]");
    	System.out.println("autootpReturnDomainValidationToken [" + dbDomainValidToken + "]");
    	System.out.println("autootpServerSettingAppServerKey [" + dbSecretKey + "]");
    	System.out.println("autootpServerSettingAuthServerDomain [" + dbAuthDomain + "]");
    	
    	attributes.put("nowDate", dateTime);
    	attributes.put("dbBrowserFlowAlias", dbBrowserFlowAlias);
    	attributes.put("autootpAppSettingDomain", dbDomain);
    	attributes.put("autootpAppSettingEmail", dbEmail);
    	attributes.put("autootpAppSettingIpAddress", dbIpAddr);
    	attributes.put("autootpAppSettingName", dbName);
    	attributes.put("autootpAppSettingProxyServerDomain", dbProxyDomain);
    	attributes.put("autootpAppSettingStep", dbStep);
    	attributes.put("autootpReturnDomainValidationToken", dbDomainValidToken);
    	attributes.put("autootpServerSettingAppServerKey", dbSecretKey);
    	attributes.put("autootpServerSettingAuthServerDomain", dbAuthDomain);
    	
    	System.out.println(".......................................................");
        
        System.out.println("isEnabled [" + strIsEnabled + "]");
        System.out.println("isEmailVerified [" + strIsEmailVerified + "]");
        System.out.println("userId [" + userId + "]");
        System.out.println("username [" + username + "]");
        System.out.println("firstName [" + firstName + "]");
        System.out.println("lastName [" + lastName + "]");
        System.out.println("email [" + email + "]");
        
        attributes.put("isEnabled", strIsEnabled);
        attributes.put("isEmailVerified", strIsEmailVerified);
        attributes.put("userId", userId);
        attributes.put("username", username);
        attributes.put("firstName", firstName);
        attributes.put("lastName", lastName);
        attributes.put("email", email);
        
        System.out.println(".......................................................");

        String link = "realms/" + realm.getName() + "/login-actions/autootp-regist";
        KeycloakUriInfo uriInfo = session.getContext().getUri();
    	link = uriInfo.getBaseUri() + link;
    	attributes.put("autootpLink", link);
    	
    	
    	String baseUrl = (String) attributes.get("baseUrl");
    	
    	//long expirationInMinutes = realm.getAccessCodeLifespanUserAction() / 60;
    	long expirationInMinutes = realm.getActionTokenGeneratedByUserLifespan() / 60;
    	System.out.println("^^^^^^^^^^^^^^^ [" + baseUrl + "] " + realm.getUserActionTokenLifespans() + " / " + realm.getActionTokenGeneratedByUserLifespan());
    	
    	
    	//String autootpRegParam = dateTime + "&username=" + username + "&secretKey=" + dbSecretKey + "&authDomain=" + URLEncode(dbAuthDomain);
    	String autootpRegParam = dateTime + "|||" + expirationInMinutes + "|||" + username + "|||" + URLEncode(dbAuthDomain) + "|||" + URLEncode(baseUrl);
    	String encParam = getEncryptAES(autootpRegParam, dbSecretKey.getBytes());
    	encParam = encParam.replaceAll("\\+", "_");
    	attributes.put("autootpRegParam", encParam);
    	
    	String strExpirationInMinutes = format(expirationInMinutes * 60);
    	attributes.put("strExpiration", strExpirationInMinutes);

    	
        return this;
    }

    @Override
    public EmailTemplateProvider setAttribute(String name, Object value) {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: setAttribute(" + name + "," + value + ")");
        attributes.put(name, value);
        return this;
    }

    @Override
    public EmailTemplateProvider setAuthenticationSession(AuthenticationSessionModel authenticationSession) {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: setAuthenticationSession()");
        this.authenticationSession = authenticationSession;
        return this;
    }

    protected String getRealmName() {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: getRealmName()");
        if (realm.getDisplayName() != null) {
            return realm.getDisplayName();
        } else {
            return ObjectUtil.capitalize(realm.getName());
        }
    }

    @Override
    public void sendEvent(Event event) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: sendEvent()");
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        attributes.put("event", new EventBean(event));

        send(toCamelCase(event.getType()) + "Subject", "event-" + event.getType().toString().toLowerCase() + ".ftl", attributes);
    }

    @Override
    public void sendPasswordReset(String link, long expirationInMinutes) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: sendPasswordReset(" + link + ", " + expirationInMinutes + ")");
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        send("passwordResetSubject", "password-reset.ftl", attributes);
    }

    @Override
    public void sendSmtpTestEmail(Map<String, String> config, UserModel user) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: sendSmtpTestEmail()");
        setRealm(session.getContext().getRealm());
        setUser(user);

        Map<String, Object> attributes = new HashMap<>(this.attributes);

        EmailTemplate email = processTemplate("emailTestSubject", Collections.emptyList(), "email-test.ftl", attributes);
        send(config, email.getSubject(), email.getTextBody(), email.getHtmlBody());
    }

    @Override
    public void sendConfirmIdentityBrokerLink(String link, long expirationInMinutes) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: sendConfirmIdentityBrokerLink()");
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        BrokeredIdentityContext brokerContext = (BrokeredIdentityContext) this.attributes.get(IDENTITY_PROVIDER_BROKER_CONTEXT);
        String idpAlias = brokerContext.getIdpConfig().getAlias();
        String idpDisplayName = brokerContext.getIdpConfig().getDisplayName();
        idpAlias = ObjectUtil.capitalize(idpAlias);
        String displayName = idpAlias;
        if (!ObjectUtil.isBlank(brokerContext.getIdpConfig().getDisplayName())) {
            displayName = brokerContext.getIdpConfig().getDisplayName();
        }

        if (idpDisplayName != null && idpDisplayName.length() > 0) {
            idpAlias = ObjectUtil.capitalize(idpDisplayName);
        }

        attributes.put("identityProviderContext", brokerContext);
        attributes.put("identityProviderAlias", idpAlias);
        attributes.put("identityProviderDisplayName", displayName);

        List<Object> subjectAttrs = Arrays.asList(displayName);
        send("identityProviderLinkSubject", subjectAttrs, "identity-provider-link.ftl", attributes);
    }

    @Override
    public void sendExecuteActions(String link, long expirationInMinutes) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: sendExecuteActions()");
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        send("executeActionsSubject", "executeActions.ftl", attributes);
    }

    @Override
    public void sendVerifyEmail(String link, long expirationInMinutes) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: sendVerifyEmail(" + link + ", " + expirationInMinutes + ")");
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        send("emailVerificationSubject", "email-verification.ftl", attributes);
    }
    
    @Override
    public void sendAutoOTPEmail(String link, String username, String dbSecretKey, String dbAuthDomain, long expirationInMinutes, String addr, String baseUrl) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: sendAutoOTPEmail(" + link + ", " + user + ", " + expirationInMinutes + ", " + addr + ", " + baseUrl + ")");

    	setRealm(session.getContext().getRealm());
        //setUser(user);
    	
    	KeycloakUriInfo uriInfo = session.getContext().getUri();
    	link = uriInfo.getBaseUri() + link;
    	attributes.put("autootpLink", link);
    	
    	Map<String, Object> attributes = new HashMap<>(this.attributes);
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);
        attributes.put("addr", addr);
        
        
        String dateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        //String autootpRegParam = "nowDate=" + dateTime + "&username=" + username + "&secretKey=" + dbSecretKey + "&authDomain=" + URLEncode(dbAuthDomain);
        String autootpRegParam = dateTime + "|||" + expirationInMinutes + "|||" + username + "|||" + URLEncode(dbAuthDomain) + "|||" + URLEncode(baseUrl);
        String encParam = getEncryptAES(autootpRegParam, dbSecretKey.getBytes());
        encParam = encParam.replaceAll("\\+", "_");
    	attributes.put("autootpRegParam", encParam);
    	
    	String strExpirationInMinutes = format(expirationInMinutes * 60);
    	attributes.put("strExpiration", strExpirationInMinutes);
        
    	
        //send("emailAutoOTPSubject", "email-autootp.ftl", attributes);
        send("emailAutoOTPSubject", Collections.emptyList(), "email-autootp.ftl", attributes, addr);
    }

    @Override
    public void sendEmailUpdateConfirmation(String link, long expirationInMinutes, String newEmail) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: sendEmailUpdateConfirmation(" + link + ", " + expirationInMinutes + "," + newEmail + ")");
        if (newEmail == null) {
            throw new IllegalArgumentException("The new email is mandatory");
        }

        Map<String, Object> attributes = new HashMap<>(this.attributes);
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);
        attributes.put("newEmail", newEmail);

        send("emailUpdateConfirmationSubject", Collections.emptyList(), "email-update-confirmation.ftl", attributes, newEmail);
    }

    /**
     * Add link info into template attributes.
     *
     * @param link to add
     * @param expirationInMinutes to add
     * @param attributes to add link info into
     */
    protected void addLinkInfoIntoAttributes(String link, long expirationInMinutes, Map<String, Object> attributes) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: addLinkInfoIntoAttributes()");
        attributes.put("link", link);
        attributes.put("linkExpiration", expirationInMinutes);
        try {
            Locale locale = session.getContext().resolveLocale(user);
            attributes.put("linkExpirationFormatter", new LinkExpirationFormatterMethod(getTheme().getMessages(locale), locale));
        } catch (IOException e) {
            throw new EmailException("Failed to template email", e);
        }
    }

    @Override
    public void send(String subjectFormatKey, String bodyTemplate, Map<String, Object> bodyAttributes) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: send1()");
        send(subjectFormatKey, Collections.emptyList(), bodyTemplate, bodyAttributes);
    }

    protected EmailTemplate processTemplate(String subjectKey, List<Object> subjectAttributes, String template, Map<String, Object> attributes) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: processTemplate(" + subjectKey + ")");
        try {
            Theme theme = getTheme();
            Locale locale = session.getContext().resolveLocale(user);
            attributes.put("locale", locale);
            KeycloakUriInfo uriInfo = session.getContext().getUri();
            Properties rb = new Properties();
            
            System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: processTemplate --- 1");
            
            if(!StringUtil.isNotBlank(realm.getDefaultLocale()))
            {
                rb.putAll(realm.getRealmLocalizationTextsByLocale(realm.getDefaultLocale()));
            }
            rb.putAll(theme.getMessages(locale));
            rb.putAll(realm.getRealmLocalizationTextsByLocale(locale.toLanguageTag()));
            attributes.put("msg", new MessageFormatterMethod(locale, rb));
            attributes.put("properties", theme.getProperties());
            attributes.put("realmName", getRealmName());
            
            System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: processTemplate --- 2");
            
            if(subjectKey.equals("emailAutoOTPSubject")) {
            }
            else {
            	attributes.put("user", new ProfileBean(user));
            }
            
            attributes.put("url", new UrlBean(realm, theme, uriInfo.getBaseUri(), null));
            
            System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: processTemplate --- 2.1 uriInfo.getBaseUri [" + uriInfo.getBaseUri() +"]");
            
            String subject = new MessageFormat(rb.getProperty(subjectKey, subjectKey), locale).format(subjectAttributes.toArray());
            
            System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: processTemplate --- 2.2 subject [" + subject + "]");
            
            String textTemplate = String.format("text/%s", template);
            
            System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: processTemplate --- 2.3 textTemplate [" + textTemplate + "]");

        	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: processTemplate --- 2.4 theme [" + theme.toString() + "] attributes [" + attributes.toString() + "]");
        	
            String textBody;
            if(subjectKey.equals("emailAutoOTPSubject")) {
            	textBody = "";
            }
            else {
                try {
                    textBody = freeMarker.processTemplate(attributes, textTemplate, theme);
                } catch (final FreeMarkerException e) {
                    throw new EmailException("Failed to template plain text email.", e);
                }
            }
            
            System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: processTemplate --- 3 textBody [" + textBody + "]");
            
            String htmlTemplate = String.format("html/%s", template);
            
            System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: processTemplate --- 3.1 htmlTemplate [" + htmlTemplate + "]");

            String htmlBody;
            try {
                htmlBody = freeMarker.processTemplate(attributes, htmlTemplate, theme);
            } catch (final FreeMarkerException e) {
                throw new EmailException("Failed to template html email.", e);
            }
            
            System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: processTemplate --- 4 htmlBody [" + htmlBody + "]");

            return new EmailTemplate(subject, textBody, htmlBody);
        } catch (Exception e) {
            throw new EmailException("Failed to template email", e);
        }
    }

    protected Theme getTheme() throws IOException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: getTheme()");
        return session.theme().getTheme(Theme.Type.EMAIL);
    }

    @Override
    public void send(String subjectFormatKey, List<Object> subjectAttributes, String bodyTemplate, Map<String, Object> bodyAttributes) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: send2()");
        send(subjectFormatKey, subjectAttributes, bodyTemplate, bodyAttributes, null);
    }

    protected void send(String subjectFormatKey, List<Object> subjectAttributes, String bodyTemplate, Map<String, Object> bodyAttributes, String address) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: send3()");
        try {
            EmailTemplate email = processTemplate(subjectFormatKey, subjectAttributes, bodyTemplate, bodyAttributes);
            send(email.getSubject(), email.getTextBody(), email.getHtmlBody(), address);
        } catch (EmailException e) {
            throw e;
        } catch (Exception e) {
            throw new EmailException("Failed to template email", e);
        }
    }

    protected void send(String subject, String textBody, String htmlBody, String address) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: send4()");
        send(realm.getSmtpConfig(), subject, textBody, htmlBody, address);
    }

    protected void send(Map<String, String> config, String subject, String textBody, String htmlBody) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: send5()");
        send(config, subject, textBody, htmlBody, null);
    }

    protected void send(Map<String, String> config, String subject, String textBody, String htmlBody, String address) throws EmailException {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: send6()");
        EmailSenderProvider emailSender = session.getProvider(EmailSenderProvider.class);
        if (address == null) {
            emailSender.send(config, user, subject, textBody, htmlBody);
        } else {
            emailSender.send(config, address, subject, textBody, htmlBody);
        }
    }

    @Override
    public void close() {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: close()");
    }

    protected String toCamelCase(EventType event) {
    	System.out.println("xxxxxxxxxxxxxxxxxxxx FreeMarkerEmailTemplateProvider :: toCamelCase()");
        StringBuilder sb = new StringBuilder("event");
        for (String s : event.name().toLowerCase().split("_")) {
            sb.append(ObjectUtil.capitalize(s));
        }
        return sb.toString();
    }

    protected static class EmailTemplate {

        private String subject;
        private String textBody;
        private String htmlBody;

        public EmailTemplate(String subject, String textBody, String htmlBody) {
            this.subject = subject;
            this.textBody = textBody;
            this.htmlBody = htmlBody;
        }

        public String getSubject() {
            return subject;
        }

        public String getTextBody() {
            return textBody;
        }

        public String getHtmlBody() {
            return htmlBody;
        }
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
 	
 	public String URLEncode(String param) {
    	String retVal = "";
    	
    	if(param != null) {
	    	try {
	            retVal = URLEncoder.encode(param, "UTF-8");
	        } catch (UnsupportedEncodingException e1) {
	            e1.printStackTrace();
	        }
    	}
    	
    	return retVal;
    }
 	
 	protected String format(long valueInSeconds) {

        String unitKey = "seconds";
        long value = valueInSeconds;

        if (value > 0 && value % 60 == 0) {
            unitKey = "minutes";
            value = value / 60;
            if (value % 60 == 0) {
                unitKey = "hours";
                value = value / 60;
                if (value % 24 == 0) {
                    unitKey = "days";
                    value = value / 24;
                }
            }
        }

        return value + " " + unitKey;
    }
}
