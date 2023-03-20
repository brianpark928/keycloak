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
package org.keycloak.forms.login.freemarker.model;

import org.keycloak.authentication.actiontoken.idpverifyemail.IdpVerifyAccountLinkActionToken;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionToken;
import org.keycloak.authentication.actiontoken.verifyemail.VerifyEmailActionToken;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.CredentialRepresentation;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.keycloak.models.AuthenticationFlowModel;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class RealmBean {

    private RealmModel realm;

    public RealmBean(RealmModel realmModel) {
        realm = realmModel;
    }

    public String getAttributeautootpAppSettingDomain() {
        return realm.getAttribute("autootpAppSettingDomain");
    }

    public String getAttributeautootpAppSettingEmail() {
        return realm.getAttribute("autootpAppSettingEmail");
    }
    
    public String getAttributeautootpAppSettingIpAddress() {
        return realm.getAttribute("autootpAppSettingIpAddress");
    }
    
    public String getAttributeautootpAppSettingName() {
        return realm.getAttribute("autootpAppSettingName");
    }
    
    public String getAttributeautootpAppSettingProxyServerDomain() {
        return realm.getAttribute("autootpAppSettingProxyServerDomain");
    }
    
    public String getAttributeautootpAppSettingStep() {
        return realm.getAttribute("autootpAppSettingStep");
    }
    
    public String getAttributeautootpReturnDomainValidationToken() {
        return realm.getAttribute("autootpReturnDomainValidationToken");
    }
    
    public String getAttributeautootpReturnServerProgress() {
        return realm.getAttribute("autootpReturnServerProgress");
    }
    
    public String getAttributeautootpServerSettingAppServerKey() {
        return realm.getAttribute("autootpServerSettingAppServerKey");
    }
    
    public String getAttributeautootpServerSettingAuthServerDomain() {
        return realm.getAttribute("autootpServerSettingAuthServerDomain");
    }
    
    public String getAttributeautootpAppSettingPublickey() {
        return realm.getAttribute("autootpAppSettingPublickey");
    }
    
    public String getAttributeautootpAppSettingPrivatekey() {
        return realm.getAttribute("autootpAppSettingPrivatekey");
    }

    public String getBrowserFlowId() {
    	AuthenticationFlowModel flowModel = realm.getBrowserFlow();
    	return flowModel.getId();
    }

    public String getBrowserFlowAlias() {
    	AuthenticationFlowModel flowModel = realm.getBrowserFlow();
    	return flowModel.getAlias();
    }

    public String getName() {
        return realm.getName();
    }

    public String getDisplayName() {
        String displayName = realm.getDisplayName();
        if (displayName != null && displayName.length() > 0) {
            return displayName;
        } else {
            return getName();
        }
    }

    public String getDisplayNameHtml() {
        String displayNameHtml = realm.getDisplayNameHtml();
        if (displayNameHtml != null && displayNameHtml.length() > 0) {
            return displayNameHtml;
        } else {
            return getDisplayName();
        }
    }

    public boolean isIdentityFederationEnabled() {
        return realm.isIdentityFederationEnabled();
    }

    public boolean isRegistrationAllowed() {
        return realm.isRegistrationAllowed();
    }

    public boolean isRegistrationEmailAsUsername() {
        return realm.isRegistrationEmailAsUsername();
    }
    
    public boolean isLoginWithEmailAllowed() {
        return realm.isLoginWithEmailAllowed();
    }

    public boolean isDuplicateEmailsAllowed() {
        return realm.isDuplicateEmailsAllowed();
    }

    public boolean isResetPasswordAllowed() {
        return realm.isResetPasswordAllowed();
    }

    public boolean isRememberMe() {
        return realm.isRememberMe();
    }

    public boolean isInternationalizationEnabled() {
        return realm.isInternationalizationEnabled();
    }

    public boolean isEditUsernameAllowed() {
        return realm.isEditUsernameAllowed();
    }

    public boolean isPassword() {
        return realm.getRequiredCredentialsStream()
                .anyMatch(r -> Objects.equals(r.getType(), CredentialRepresentation.PASSWORD));
    }

    /**
     * Returns the lifespan for action tokens generated by users in minutes.
     * @return
     */
    public int getActionTokenGeneratedByUserLifespanMinutes() {
        return (int)TimeUnit.SECONDS.toMinutes(realm.getActionTokenGeneratedByUserLifespan());
    }

    /**
     * Returns the lifespan for {@link VerifyEmailActionToken} in minutes.
     * @return
     */
    public int getVerifyEmailActionTokenLifespanMinutes() {
        return (int)TimeUnit.SECONDS.toMinutes(realm.getActionTokenGeneratedByUserLifespan(VerifyEmailActionToken.TOKEN_TYPE));
    }

    /**
     * Returns the lifespan for {@link ResetCredentialsActionToken} in minutes.
     * @return
     */
    public int getResetCredentialsActionTokenLifespanMinutes() {
        return (int)TimeUnit.SECONDS.toMinutes(realm.getActionTokenGeneratedByUserLifespan(ResetCredentialsActionToken.TOKEN_TYPE));
    }

    /**
     * Returns the lifespan for {@link IdpVerifyAccountLinkActionToken} in minutes.
     * @return
     */
    public int getIdpVerifyAccountLinkActionTokenLifespanMinutes() {
        return (int)TimeUnit.SECONDS.toMinutes(realm.getActionTokenGeneratedByUserLifespan(IdpVerifyAccountLinkActionToken.TOKEN_TYPE));
    }
}
