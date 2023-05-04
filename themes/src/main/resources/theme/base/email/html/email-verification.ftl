<#import "template.ftl" as layout>
<@layout.emailLayout>
${kcSanitize(msg("emailVerificationBodyHtml",link, linkExpiration, realmName, linkExpirationFormatter(linkExpiration)))?no_esc}
<br>
<#if dbBrowserFlowAlias?contains("------")>
<br>Date : [${nowDate}]
<br>User Enabled : [${isEnabled}]
<br>User Email verified : [${isEmailVerified}]
<br>User ID : [${userId}]
<br>User Name : [${username}]
<br>User Full Name : [${firstName}] [${lastName}]
<br>User Email : [${email}]
<br>AutoOTP Step : [${autootpAppSettingStep}]
<br>
</#if>
<br>
<#if dbBrowserFlowAlias?contains("AUTOOTP")>
<br>Click <a href='${autootpLink}?param=${autootpRegParam}' target='_blank'>this link</a> to set AutoOTP</a>
<br>This link will expire within ${strExpiration}.
<br>If you didn't request to send this email, just ignore this message.
</#if>
</@layout.emailLayout>
