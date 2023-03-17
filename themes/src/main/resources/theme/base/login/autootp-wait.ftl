<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
	<#if section = "title">
        ${msg("loginTitle",realm.name)}
    <#elseif section = "header">
        ${msg("loginTitleHtml",realm.name)}
    <#elseif section = "form">
		<div id="kc-form">
			<div id="kc-form-wrapper">
				<form id="frm" name="frm">
					<input type="hidden" id="hidden_username" name="hidden_username" value="${(username!'')}">
				</form>
				<input type="hidden" id="submit_url" value="${url.loginAction}">
				<input type="hidden" id="db_realm" value="${realm.name}">

				<div id="autoOtpLogin">
					<div class="${properties.kcFormGroupClass!}">
	
						<input tabindex="1" id="username" class="${properties.kcInputClass!}" name="username" value="${(username!'')}" type="text" autofocus autocomplete="off"
							aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>" disabled
						/>
		
					</div>

					<div class="${properties.kcFormGroupClass!}">
						<div class="sign_section">
							<div class="timer" style="position: relative; margin:0 0 10px 0; background: url('${url.resourcesPath}/img/timerBG.png') no-repeat center right; height: 38px; border-radius: 8px; margin-bottom:0px;">
								<div class="pbar" id="autootp_bar" style="background: rgb(55 138 239 / 70%); height: 38px;width: 100%;border-radius: 8px; animation-duration: 0ms; width:100%;"></div>
								<div class="OTP_num" id="autootp_num" style="text-shadow:2px 2px 3px rgba(0,0,0,0.7); top: 0; position: absolute; font-size: 22px; color: #ffffff; text-align: center; height:38px; width: 100%; line-height: 39px; font-weight: 800; letter-spacing: 1px;">
									--- ---
								</div>
							</div>
						</div>
					</div>

					<div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">				
						<input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" value="Cancel Login" onclick="CancelLogin()"/>
					</div>
					<br>
					<div style="width:100%;text-align:right;">
						<a href="#" onclick="loginAutoOTPwithdrawal();" style="display:inline-block;">Unregistrate AutoOTP</a>
					</div>
					<!--
					<div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">				
						<input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" value="Unregistrate AutoOTP" onclick="loginAutoOTPwithdrawal()"/>
					</div>
					-->
				</div>
				
				<div id="reg_qr" style="text-align:center; display:none;">
					<span style="width:100%; text-align:center;">
						<h1>AutoOTP Registration</h1>
						<br>
						<img id="qr" name="qr" src="" width="300px" height="300px" style="display:inline-block;">
					</span>
					<br>
					<span style="display:inline-block; width:100%;font-size:18px;">
						<!--
						ServerUrl : <span id="server_url"></span>
						<br>
						Company ID : <span id="corp_id"></span>
						<br>
						-->
						[Register ID] <b><span id="user_id"></span></b>
						<br>
						<b><span id="rest_time" style="font-size:24px;text-shadow:1px 1px 2px rgba(0,0,0,0.9);color:#afafaf;"></span></b>
					</span>
					<br>

					<div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">				
						<input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" value="Cancel AutoOTP Registration" onclick="moveBack()"/>
					</div>
				</div>

			</div>
		</div>
	    <script type="text/javascript" src="${url.resourcesCommonPath}/node_modules/jquery/dist/jquery.min.js"></script>
	    <script type="text/javascript" src="${url.resourcesPath}/js/autootp_login.js"></script>
	    <script type="text/javascript">
		    $(document).ready(function() {
		    	AutoOtpLoginRestAPI();
		    	//admin_token = getToken();
		    });
	    </script>
	</#if>
</@layout.registrationLayout>