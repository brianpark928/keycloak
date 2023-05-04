<!DOCTYPE html>
<html class="${properties.kcHtmlClass!}">

<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">
    
    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
        </#list>
    </#if>
    <title>AutoOTP Registration</title>
    <link rel="icon" href="${resourcesPath}/img/favicon.ico" />
    <#if properties.stylesCommon?has_content>
        <#list properties.stylesCommon?split(' ') as style>
            <link href="${resourcesCommonPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>
</head>

<body class="${properties.kcBodyClass!}">
<input type="hidden" id="realmName" name="realmName" value="${realm.name}">
<input type="hidden" id="gapMinute" name="gapMinute" value="${gapMinute}">
<input type="hidden" id="expirationInMinutes" name="expirationInMinutes" value="${expirationInMinutes}">
<input type="hidden" id="username" name="username" value="${username}">
<input type="hidden" id="baseUrl" name="baseUrl" value="${baseUrl}">
<input type="hidden" id="authDomain" name="authDomain" value="${dbAuthDomain}">

<div class="${properties.kcLoginClass!}">
    <div id="kc-header" class="${properties.kcHeaderClass!}">
        <div id="kc-header-wrapper"
             class="${properties.kcHeaderWrapperClass!}">${realm.name}</div>
    </div>
    
    <div class="card-pf" id="autootp_expiration" name="autootp_expiration" style="display:none;">
        <header class="login-pf-header">
        	<h1 id="kc-page-title">You need to re-send AutoOTP setting email.</h1>
        </header>
        <div id="kc-content">
        	<div id="kc-content-wrapper">
        		<div id="kc-error-meesage">
        			<p class="instruction">Email expired.</p>
        			<p>
        				<a id="backToApplication" href="${baseUrl}">« Back to Application</a>
        			</p>
        		</div>
        	</div>
        </div>
    </div>
    
    <div class="card-pf" id="userinfo_empty" name="userinfo_empty" style="display:none;">
        <header class="login-pf-header">
        	<h1 id="kc-page-title">You need to re-send AutoOTP setting email.</h1>
        </header>
        <div id="kc-content">
        	<div id="kc-content-wrapper">
        		<div id="kc-error-meesage">
        			<p class="instruction">User infomation is empty.</p>
        			<p>
        				<a id="backToApplication" href="${baseUrl}">« Back to Application</a>
        			</p>
        		</div>
        	</div>
        </div>
    </div>
    
    <div class="${properties.kcFormCardClass!}" id="autootp_content" name="autootp_content" style="display:none;">

		<div id="reg_qr" style="text-align:center; display:none;">
			<div>
				<span style="width:100%; text-align:center;">
					<h1>AutoOTP registration</h1>
					<br>
					<img id="qr" name="qr" src="" width="300px" height="300px" style="display:inline-block;">
				</span>
				<br>
				<span style="display:inline-block; width:100%;font-size:18px;">
					[Register ID] <b><span id="user_id"></span></b>
					<br>
					<b><span id="rest_time" style="font-size:24px;text-shadow:1px 1px 2px rgba(0,0,0,0.9);color:#afafaf;"></span></b>
				</span>
				<!--
				<br>
				<br>
				<a href="javascript:moveHome();"><h3>Cancel</h3></a>
				-->
			</div>
		</div>

		<div id="cancel_qr" style="text-align:center; display:none;">
			<span style="display:inline-block;">
				<h3>Are you sure you want to unregister AutoOTP?</h3>
			</span>
			<br>
			<br>
			
			<div id="autootp_login" class="${properties.kcFormGroupClass!}">				
				<input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" value="Unregister AutoOTP" onclick="loginAutoOTPwithdrawal();"/>
			</div>
		</div>

		<!--		
		<div id="move_home_btn" name="move_home_btn" style="width:100%;text-align:right;">
			<br>
			<a href="#" onclick="moveHome();" style="display:inline-block;">Back to home</a>
		</div>
		-->
        <div id="kc-content">
        	<div id="kc-content-wrapper">
       			<p>
       				<a id="backToApplication" href="${baseUrl}">« Back to Application</a>
       			</p>
        	</div>
        </div>

	</div>
	<script type="text/javascript" src="${resourcesCommonPath}/node_modules/jquery/dist/jquery.min.js"></script>
    <script type="text/javascript" src="${resourcesPath}/js/autootp_regist.js"></script>
    <script type="text/javascript">
	    $(document).ready(function() {
	    	AutoOTPRegist();
	    });
    </script>
</div>
</body>
</html>
	