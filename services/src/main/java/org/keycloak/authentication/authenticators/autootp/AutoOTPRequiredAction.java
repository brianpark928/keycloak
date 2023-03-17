/*
 * Copyright 2023 eStorm Inc
 */

package org.keycloak.authentication.authenticators.autootp;

import org.keycloak.authentication.CredentialRegistrator;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.authentication.authenticators.autootp.credential.AutoOTPModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.Response;

import org.keycloak.models.KeycloakSession;

/**
 * @author <a href="mailto:aaa@bbb.ccc">xXx xxXXxx</a>
 * @version $Revision: 1 $
 */
public class AutoOTPRequiredAction implements RequiredActionProvider, CredentialRegistrator {
    public static final String PROVIDER_ID = "autootp_wait";
    
    @Override
    public void evaluateTriggers(RequiredActionContext context) {

    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
    	System.out.println(">>>>>>>>>>>>>>>>>>>>> AutoOTPRequiredAction :: requiredActionChallenge");
    	
    	UserModel user = context.getUser();
    	String username = user.getUsername();

    	System.out.println("username [" + username + "]");

        Response challenge = context.form()
        					.setAttribute("username", username)
        					.createForm("autootp-wait.ftl");
        context.challenge(challenge);
    }

    @Override
    public void processAction(RequiredActionContext context) {
    	System.out.println(">>>>>>>>>>>>>>>>>>>>> AutoOTPRequiredAction :: processAction");
    	/*
        String answer = (context.getHttpRequest().getDecodedFormParameters().getFirst("secret_answer"));
        AutoOTPCredentialProvider sqcp = (AutoOTPCredentialProvider) context.getSession().getProvider(CredentialProvider.class, "secret-question");
        sqcp.createCredential(context.getRealm(), context.getUser(), AutoOTPCredentialModel.createAutoOTP("What is your mom's first name?", answer));
        context.success();
        */

    	/*
    	String corpId = "1c7b5fcfb68e453abf1c98b27d2aa362";
    	System.out.println("corpId = " + corpId);
    	
        AutoOTPCredentialProvider autootpcp = (AutoOTPCredentialProvider) context.getSession().getProvider(CredentialProvider.class, "autootp");
        autootpcp.createCredential(context.getRealm(), context.getUser(), AutoOTPModel.createAutoOTP(corpId));
        context.success();
        */
        String username = (context.getHttpRequest().getDecodedFormParameters().getFirst("hidden_username"));
        System.out.println("hidden_username [" + username + "]");
        context.success();
    }

    @Override
    public void close() {

    }
}
