<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
	<#if section = "header">
		${msg("loginAccountTitle")}
	<#elseif section = "form">
	<div id="kc-form">
		<div id="kc-form-wrapper">
			<!--
			Realm - name [${realm.name!''}] / displayName [${realm.displayName!''}]
			<br>[${realm.attributeautootpAppSettingDomain!"attributeautootpAppSettingDomain empty!"}]
			<br>[${realm.attributeautootpAppSettingEmail!"attributeautootpAppSettingEmail empty!"}]
			<br>[${realm.attributeautootpAppSettingIpAddress!"attributeautootpAppSettingIpAddress empty!"}]
			<br>[${realm.attributeautootpAppSettingName!"attributeautootpAppSettingName empty!"}]
			<br>[${realm.attributeautootpAppSettingProxyServerDomain!"attributeautootpAppSettingProxyServerDomain empty!"}]
			<br>[${realm.attributeautootpAppSettingStep!"attributeautootpAppSettingStep empty!"}]
			<br>[${realm.attributeautootpReturnDomainValidationToken!"attributeautootpReturnDomainValidationToken empty!"}]
			<br>[${realm.attributeautootpReturnServerProgress!"attributeautootpReturnServerProgress empty!"}]
			<br>[${realm.attributeautootpServerSettingAppServerKey!"attributeautootpServerSettingAppServerKey empty!"}]
			<br>[${realm.attributeautootpServerSettingAuthServerDomain!"attributeautootpServerSettingAuthServerDomain empty!"}]
			<br>[${realm.browserFlowId!"browserFlowId empty!"}]
			<br>[${realm.browserFlowAlias!"browserFlowAlias empty!"}]
			-->
			<#if realm.password>
	        
	        	<input type="hidden" id="browser_flow_id" name="browser_flow_id" value="${realm.browserFlowAlias!''}">
	        	<input type="hidden" id="submit_url" name="submit_url" value="${url.loginAction}">
	        	
	            <form id="kc-form-login" name="kc-form-login" onsubmit="" action="" method="post">

					<input type="hidden" id="base_url" name="base_url" value="${client.baseUrl}">
					<input type="hidden" id="page_set" name="page_set" value="login">
					<input type="hidden" id="page_config" name="page_config" value="">
					<input type="hidden" id="login_realm" name="login_realm" value="${realm.name!''}">
					<input type="hidden" id="login_step" name="login_step" value="${realm.attributeautootpAppSettingStep!''}">
					<input type="hidden" id="login_flow" name="login_flow" value="">
					<input type="hidden" id="autootp_info" name="autootp_info" value="">

	                <#if !usernameHidden??>
	                    <div class="${properties.kcFormGroupClass!}">
	                        <label for="username" class="${properties.kcLabelClass!}"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></label>
	
	                        <input tabindex="1" id="username" class="${properties.kcInputClass!}" name="username" value="${(login.username!'')}"  type="text" autofocus autocomplete="off"
	                               aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
	                        />
	
	                        <#if messagesPerField.existsError('username','password')>
	                            <span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
	                                    ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
	                            </span>
	                        </#if>
	
	                    </div>
	                </#if>



					<div id="login_password">
	
		                <div class="${properties.kcFormGroupClass!}">
		                    <label for="password" class="${properties.kcLabelClass!}">${msg("password")}</label>
		
		                    <input tabindex="2" id="password" class="${properties.kcInputClass!}" name="password" type="password" autocomplete="off"
		                           aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
		                    />
		
		                    <#if usernameHidden?? && messagesPerField.existsError('username','password')>
		                        <span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
		                                ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
		                        </span>
		                    </#if>
		
		                </div>
		
						<div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
							<div id="kc-form-options">
							<#if realm.rememberMe && !usernameHidden??>
								<div class="checkbox">
									<label>
										<#if login.rememberMe??>
											<input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" checked> ${msg("rememberMe")}
										<#else>
											<input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox"> ${msg("rememberMe")}
										</#if>
									</label>
								</div>
							</#if>
								</div>
								<div class="${properties.kcFormOptionsWrapperClass!}">
									<#if realm.resetPasswordAllowed>
										<span><a tabindex="5" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
									</#if>
								</div>
		
						</div>
		
						<div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
							<input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
							<input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
						</div>
					</div>

					<div id="login_autootp" class="${properties.kcFormGroupClass!}">
						<div class="sign_section">
							<div class="timer" style="position: relative; margin:0 0 10px 0; background: url('${url.resourcesPath}/img/timerBG.png') no-repeat center right; height: 38px; border-radius: 8px; margin-bottom:0px;">
								<div class="pbar" id="autootp_bar" style="background: rgb(55 138 239 / 70%); height: 38px;width: 100%;border-radius: 8px; animation-duration: 0ms; width:100%;"></div>
								<div class="OTP_num" id="autootp_num" style="text-shadow:2px 2px 3px rgba(0,0,0,0.7); top: 0; position: absolute; font-size: 22px; color: #ffffff; text-align: center; height:38px; width: 100%; line-height: 39px; font-weight: 800; letter-spacing: 1px;">
									--- ---
								</div>
							</div>
						</div>
						<br>
						<div id="autootp_login" class="${properties.kcFormGroupClass!}">				
							<input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="autootp_login_btn" id="autootp_login_btn" value="AutoOTP Sign In" onclick="AutoOTPLogin()"/>
						</div>
						<!--
						<div style="width:100%;text-align:right;">
							<a href="#" onclick="loginAutoOTPconfigure();" style="display:inline-block;">Configure AutoOTP</a>
						</div>
						-->
						
						<div style="width:100%;text-align:right;">
					    	<a href="#" onclick="loginAutoOTPconfigure();" style="display:inline-block;">Send AutoOTP setting email</a>
						</div>
					</div>

					<div id="cancel_config_autootp" name="cancel_config_autootp" style="width:100%;text-align:right;display:none;">
						<br>
						<a href="#" onclick="cancelLoginAutoOTPconfigure();" style="display:inline-block;">Back to Login</a>
					</div>
						
					<div id="move_home_btn" name="move_home_btn" style="width:100%;text-align:right;display:none;">
						<br>
						<a href="#" onclick="moveHome();" style="display:inline-block;">Back to Application</a>
					</div>

					<script type="text/javascript" src="${url.resourcesCommonPath}/node_modules/jquery/dist/jquery.min.js"></script>
				    <script type="text/javascript" src="${url.resourcesPath}/js/autootp_login.js"></script>
				    <script type="text/javascript">
					    $(document).ready(function() {
					    	AutoOtpLoginRestAPI();
					    	//admin_token = getToken();
					    });
				    </script>
				    
				</form>
				
			</#if>
		</div>
    </div>
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration-container">
                <div id="kc-registration">
                    <span>${msg("noAccount")} <a tabindex="6"
                                                 href="${url.registrationUrl}">${msg("doRegister")}</a></span>
                </div>
            </div>
        </#if>
    <#elseif section = "socialProviders" >
        <#if realm.password && social.providers??>
            <div id="kc-social-providers" class="${properties.kcFormSocialAccountSectionClass!}">
                <hr/>
                <h4>${msg("identity-provider-login-label")}</h4>

                <ul class="${properties.kcFormSocialAccountListClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountListGridClass!}</#if>">
                    <#list social.providers as p>
                        <a id="social-${p.alias}" class="${properties.kcFormSocialAccountListButtonClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountGridItem!}</#if>"
                                type="button" href="${p.loginUrl}">
                            <#if p.iconClasses?has_content>
                                <i class="${properties.kcCommonLogoIdP!} ${p.iconClasses!}" aria-hidden="true"></i>
                                <span class="${properties.kcFormSocialAccountNameClass!} kc-social-icon-text">${p.displayName!}</span>
                            <#else>
                                <span class="${properties.kcFormSocialAccountNameClass!}">${p.displayName!}</span>
                            </#if>
                        </a>
                    </#list>
                </ul>
            </div>
        </#if>
    </#if>

</@layout.registrationLayout>
