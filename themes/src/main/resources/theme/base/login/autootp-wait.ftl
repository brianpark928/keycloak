<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
	<#if section = "title">
        ${msg("loginTitle",realm.name)}
    <#elseif section = "header">
        ${msg("loginTitleHtml",realm.name)}
    <#elseif section = "form">
		<div id="kc-form">
			<div id="kc-form-wrapper" style="display:none;">
			
				<input type="hidden" id="base_url" name="base_url" value="${client.baseUrl}">
				<input type="hidden" id="page_set" name="page_set" value="autootp">
				<input type="hidden" id="page_config" name="page_config" value="">
				<input type="hidden" id="browser_flow_id" name="browser_flow_id" value="${realm.browserFlowAlias!''}">
				<input type="hidden" id="login_realm" name="login_realm" value="${realm.name!''}">

        		<input type="hidden" id="login_step" name="login_step" value="${realm.attributeautootpAppSettingStep!''}">
				<input type="hidden" id="login_flow" name="login_flow" value="">
				<input type="hidden" id="submit_url" value="${url.loginAction}">
				
				<form id="frm" name="frm">
					<input type="hidden" id="hidden_username" name="hidden_username" value="${(username!'')}">
					<input type="hidden" id="autootp_info" name="autootp_info" value="${(autootp_info!'')}">
				</form>

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
					<!--
					<div style="width:100%;text-align:right;">
						<a href="#" onclick="loginAutoOTPwithdrawal('T');" style="display:inline-block;">Unregistrate AutoOTP</a>
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
						[Register ID] <b><span id="user_id"></span></b>
						<br>
						<b><span id="rest_time" style="font-size:24px;text-shadow:1px 1px 2px rgba(0,0,0,0.9);color:#afafaf;"></span></b>
					</span>
					<br>

					<!--
					<div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">				
						<input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" value="Cancel AutoOTP Registration" onclick="moveBack()"/>
					</div>
					-->
				</div>
				<div id="send_email" style="text-align:center; display:none;">
					<div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">				
						<input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="btn_autootp_email" id="btn_autootp_email" value="Send AutoOTP setting email" onclick="sendAutoOTPRegEmail('F')"/>
					</div>
				</div>

				<div id="config" style="text-align:center; display:none;">
					<span style="display:inline-block; width:100%;font-size:16px;">
						Your AutoOTP account will be unregistrated.
					</span>
					<br>
					<div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">				
						<input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="unreg_autootp" value="Unregistrate AutoOTP" onclick="loginAutoOTPwithdrawal('F')"/>
					</div>
					<!--
					<br>
					<div style="width:100%;text-align:right;">
						<a href="#" onclick="cancelWithdrawAutoOTP();" style="display:inline-block;">Back to Login</a>
					</div>
					-->
				</div>

			</div>
			<div id="loading" style="text-align:center; display:none;">
				<span style="display:inline-block; width:100%;font-size:16px;">
					Wait for loading...
				</span>
			</div>
		</div>
		<div id="txt_autootp_email" name="txt_autootp_email" style="width:100%;text-align:center; display:none;">
			Waiting for sending email...
			<br>
			&nbsp;
		</div>
		<div id="link_autootp_email" name="link_autootp_email" style="width:100%;text-align:right; display:none;">
	    	<a href="#" onclick="sendAutoOTPRegEmail('T');" style="display:inline-block;">Send AutoOTP setting email</a>
	    	<br>
	    	&nbsp;
		</div>
		<div style="width:100%;text-align:right;">
			<!--
	    	<a href="#" onclick="sendAutoOTPRegEmail();" style="display:inline-block;">AutoOTP RegEmail</a>
	    	-->
	    	<a href="#" onclick="cancelWithdrawAutoOTP();" style="display:inline-block;">Back to Login</a>
		</div>
	    <script type="text/javascript" src="${url.resourcesCommonPath}/node_modules/jquery/dist/jquery.min.js"></script>
	    <script type="text/javascript" src="${url.resourcesPath}/js/autootp_login.js"></script>
	    <script type="text/javascript">
		    $(document).ready(function() {
		    	AutoOtpLoginRestAPI();
		    });
	    </script>
	</#if>
</@layout.registrationLayout>