<#import "template.ftl" as layout>
<@layout.emailLayout>
Click <a href='${autootpLink}?param=${autootpRegParam}' target='_blank'>this link</a> for setting AutoOTP</a>
<br>This link will expire within ${strExpiration}.
<br>If you didn't request to send this email, just ignore this message.
</@layout.emailLayout>
