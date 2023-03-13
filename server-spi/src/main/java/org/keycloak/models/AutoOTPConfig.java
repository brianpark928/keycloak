/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models;

import java.util.Arrays;
import java.util.List;

import org.keycloak.jose.jws.Algorithm;
import org.keycloak.utils.StringUtil;

public class AutoOTPConfig extends AbstractConfig {

    // Constants
    public static final String AUTOOTP_1_STEP = "1-step Authentication";
    public static final String AUTOOTP_2_STEP = "2-step Authentication";

    // realm attribute names
    public static final String AUTOOTP_APPLICATION_SETTING_STEP = "autootpAppSettingStep";
    public static final String AUTOOTP_APPLICATION_SETTING_NAME = "autootpAppSettingName";
    public static final String AUTOOTP_APPLICATION_SETTING_DOMAIN = "autootpAppSettingDomain";
    public static final String AUTOOTP_APPLICATION_SETTING_IP_ADDRESS = "autootpAppSettingIpAddress";
    public static final String AUTOOTP_APPLICATION_SETTING_PROXY_SERVER_DOMAIN = "autootpAppSettingProxyServerDomain";
    public static final String AUTOOTP_APPLICATION_SETTING_EMAIL = "autootpAppSettingEmail";

    public static final String AUTOOTP_RETURN_DOMAIN_VALIDATION_TOKEN = "autootpReturnDomainValidationToken";
    public static final String AUTOOTP_RETURN_SERVER_PROGRESS = "autootpReturnServerProgress";

    public static final String AUTOOTP_SERVER_SETTING_APPLICATION_SERVER_KEY = "autootpServerSettingAppServerKey";
    public static final String AUTOOTP_SERVER_SETTING_AUTHENTICATION_SERVER_DOMAIN = "autootpServerSettingAuthServerDomain";
    
    
    // default value
    public static final String DEFAULT_AUTOOTP_APPLICATION_SETTING_STEP = AUTOOTP_1_STEP;

    // member variable
    private String appSettingStep = DEFAULT_AUTOOTP_APPLICATION_SETTING_STEP;
    private String appSettingName = null;
    private String appSettingDomain = null;
    private String appSettingIpAddress = null;
    private String appSettingProxyServerDomain = null;
    private String appSettingEmail = null;

    private String returnDomainValidationToken = null;
    private String returnServerProgress = null;

    private String serverSettingAppServerKey = null;
    private String serverSettingAuthServerDomain = null;

    
    public AutoOTPConfig(RealmModel realm) {
        this.realm = () -> realm;

        setAppSettingStep(realm.getAttribute(AUTOOTP_APPLICATION_SETTING_STEP));

        String appSettingName = realm.getAttribute(AUTOOTP_APPLICATION_SETTING_NAME);
        if (StringUtil.isNotBlank(appSettingName)) {
            setAppSettingName(appSettingName);
        }

//        String appSettingDomain = realm.getAttribute(AUTOOTP_APPLICATION_SETTING_DOMAIN);
//        if (StringUtil.isNotBlank(appSettingDomain)) {
//            setAppSettingName(appSettingDomain);
//        }

        this.realmForWrite = () -> realm;
    }

    public String getAppSettingStep() {
        return appSettingStep;
    }

    public void setAppSettingStep(String arg) {
        if (StringUtil.isBlank(arg)) {
        	arg = DEFAULT_AUTOOTP_APPLICATION_SETTING_STEP;
        }
        this.appSettingStep = arg;
        persistRealmAttribute(AUTOOTP_APPLICATION_SETTING_STEP, arg);
    }
    
    public String getAppSettingName() {
        return appSettingName;
    }

    public void setAppSettingName(String arg) {
        this.appSettingName = arg;
        persistRealmAttribute(AUTOOTP_APPLICATION_SETTING_NAME, arg);
    }

}
