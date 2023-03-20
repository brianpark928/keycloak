/*
 * Copyright 2023 eStorm Inc
 */

package org.keycloak.authentication.authenticators.autootp;

import org.keycloak.http.HttpResponse;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.CredentialValidator;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.ServerCookie;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:aaa@bbb.ccc">xXx xxXXxx</a>
 * @version $Revision: 1 $
 */
public class AutoOTPAuthenticator implements Authenticator, CredentialValidator<AutoOTPCredentialProvider> {

    protected boolean hasCookie(AuthenticationFlowContext context) {
    	System.out.println(">>>>>>>>>>>>>>>>>>>>> AutoOTPAuthenticator :: hasCookie");
    	
        Cookie cookie = context.getHttpRequest().getHttpHeaders().getCookies().get("AUTOOTP_ANSWERED");
        boolean result = cookie != null;
        if (result) {
            System.out.println("Bypassing autootp because cookie is set");
        }
        return result;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
    	System.out.println(">>>>>>>>>>>>>>>>>>>>> AutoOTPAuthenticator :: authenticate");
    	
    	UserModel userModel = context.getUser();
        String username = userModel.getUsername();

        System.out.println("userModel [" + userModel.toString() + "]");
        System.out.println("username [" + username + "]");

        /*
        if (hasCookie(context)) {
            context.success();
            return;
        }
        */
        Response challenge = context.form()
                .createForm("autootp-wait.ftl");
        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
    	System.out.println(">>>>>>>>>>>>>>>>>>>>> AutoOTPAuthenticator :: action");
    	/*
        boolean validated = validateAnswer(context);
        if (!validated) {
            Response challenge =  context.form()
                    .setError("badSecret")
                    .createForm("autootp-wait.ftl");
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }
        setCookie(context);
        context.success();
        */
    }

    protected void setCookie(AuthenticationFlowContext context) {
    	System.out.println(">>>>>>>>>>>>>>>>>>>>> AutoOTPAuthenticator :: setCookie");
    	
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        int maxCookieAge = 60 * 60 * 24 * 30; // 30 days
        if (config != null) {
            maxCookieAge = Integer.valueOf(config.getConfig().get("cookie.max.age"));

        }
        URI uri = context.getUriInfo().getBaseUriBuilder().path("realms").path(context.getRealm().getName()).build();
        addCookie(context, "AUTOOTP_ANSWERED", "true",
                uri.getRawPath(),
                null, null,
                maxCookieAge,
                false, true);
    }

    public void addCookie(AuthenticationFlowContext context, String name, String value, String path, String domain, String comment, int maxAge, boolean secure, boolean httpOnly) {
    	System.out.println(">>>>>>>>>>>>>>>>>>>>> AutoOTPAuthenticator :: addCookie");
    	
        HttpResponse response = context.getSession().getContext().getHttpResponse();
        StringBuffer cookieBuf = new StringBuffer();
        ServerCookie.appendCookieValue(cookieBuf, 1, name, value, path, domain, comment, maxAge, secure, httpOnly, null);
        String cookie = cookieBuf.toString();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie);
    }

    protected boolean validateAnswer(AuthenticationFlowContext context) {
    	System.out.println(">>>>>>>>>>>>>>>>>>>>> AutoOTPAuthenticator :: validateAnswer");
    	
    	/*
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String secret = formData.getFirst("secret_answer");
        String credentialId = formData.getFirst("credentialId");
        if (credentialId == null || credentialId.isEmpty()) {
            credentialId = getCredentialProvider(context.getSession())
                    .getDefaultCredential(context.getSession(), context.getRealm(), context.getUser()).getId();
        }

        UserCredentialModel input = new UserCredentialModel(credentialId, getType(context.getSession()), secret);
        return getCredentialProvider(context.getSession()).isValid(context.getRealm(), context.getUser(), input);
        */
    	return false;
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return getCredentialProvider(session).isConfiguredFor(realm, user, getType(session));
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        user.addRequiredAction(AutoOTPRequiredAction.PROVIDER_ID);
    }

    public List<RequiredActionFactory> getRequiredActions(KeycloakSession session) {
        return Collections.singletonList((AutoOTPRequiredActionFactory)session.getKeycloakSessionFactory().getProviderFactory(RequiredActionProvider.class, AutoOTPRequiredAction.PROVIDER_ID));
    }

    @Override
    public void close() {

    }

    @Override
    public AutoOTPCredentialProvider getCredentialProvider(KeycloakSession session) {
        return (AutoOTPCredentialProvider)session.getProvider(CredentialProvider.class, AutoOTPCredentialProviderFactory.PROVIDER_ID);
    }
}
