<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
	<#if section = "title">
        ${msg("loginTitle",realm.name)}
    <#elseif section = "header">
        Setup AutoOTP
    <#elseif section = "form">
		<div class="" style="height:400px; position:absolute; top:50%; left:50%; transform:translate(-50%, -50%);">
			<div id="cancel_qr" style="text-align:center; display:none;">
				<span style="display:inline-block;">
					<h3>등록 해지 하시겠습니까?</h3>
				</span>
				<br>
				<br>
				<span style="display:inline-block;">
					<a href="javascript:moveHome();"><b>취소</b></a>
					&nbsp;&nbsp;&nbsp;
					<a href="javascript:loginAutoOTPwithdrawal();"><b>확인</b></a>
				</span>
			</div>
			
			<div id="reg_qr" style="text-align:center; display:none;">
				<span style="width:100%; text-align:center;">
					<h1>AutoOTP 등록</h1>
					<br>
					<img id="qr" name="qr" src="" width="300px" height="300px" style="display:inline-block;">
				</span>
				<br>
				<span style="width:100%;">
					ServerUrl : <span id="server_url"></span>
					<br>
					회사 아이디 : <span id="corp_id"></span>
					<br>
					사용자 아이디 : <span id="user_id"></span>
					<br>
					남은시간 : <span id="rest_time"></span>
				</span>
				<br>
				<br>
				<a href="javascript:moveHome();"><h3>취소</h3></a>
			</div>
		</div>
	</#if>
</@layout.registrationLayout>

<script>

var autootp_terms = 0;
var autootp_millisec = 0;
var timeoutId1 = null;
var timeoutId2 = null;
var pushConnectorUrl = "";
var pushConnectorToken = "";

function showAlert() {
	alert("로그인 정보가 없습니다.\n다시 로그인 해 주세요.");
	location.href = "/Login/autoOtpReg";
}

function AutoOtpManageRestAPI() {
	/*
	 * 1. AutoOTP 등록여부 확인
	 * 2. 가입 시 해지화면, 미가입 시 등록화면 출력
	 * 3. 미가입 시 등록요청
	 * 4. 1초 주기로 가입여부 체크 --> 가입확인되면 완료처리
	 */
	 
	var isReg = checkAutoOTPReg();
	console.log("isReg = " + isReg);
	
	if(isReg == "T") {
		$("#cancel_qr").css("display", "block");
	}
	else {
		$("#reg_qr").css("display", "block");
		loginAutoOTPJoinStart();
	}
}

// 가입여부 체크
function checkAutoOTPReg() {
	//console.log("----- checkAutoOTPReg() -----");
	
	var ret_val = "";
	
	var data = {
		url: "isApUrl",
		params: "userId=<%=userId%>"
	}
	
	var result = callApi(data);
	jsonResult = JSON.parse(result);
	var exist = false;

	var code = jsonResult.code;
	if(code == "000" || code == "000.0")
		exist = jsonResult.data.exist;
	
	if(exist)	ret_val = "T";
	else		ret_val = "F";
	
	return ret_val;
}

function moveHome() {
	location.href = "<%=path%>";
}

// 해지요청
function loginAutoOTPwithdrawal() {
console.log("----- loginAutoOTPwithdrawal() -----");
	
	var data = {
		url: "withdrawalApUrl",
		params: "userId=<%=userId%>"
	}
	
	var result = callApi(data);
	console.log(result);
	jsonResult = JSON.parse(result);
	
	var code = jsonResult.code;
	if(code == "000" || code == "000.0") {
		alert("정상 해지 되었습니다.");
		moveHome();
	}
	else {
		alert("잠시후 다시 시도해 주시기 바랍니다.");
		moveHome();
	}
}

// 등록요청
function loginAutoOTPJoinStart() {
	console.log("----- loginAutoOTPJoinStart() -----");
	
	var data = {
		url: "joinApUrl",
		params: "userId=<%=userId%>&name=<%=name%>&email=<%=email%>"
	}
	
	var result = callApi(data);
	console.log(result);
	jsonResult = JSON.parse(result);
	
	var code = jsonResult.code;
	if(code == "000" || code == "000.0") {
		var data = jsonResult.data;
		var qr = data.qr;
		var corpId = data.corpId;
		var registerKey = data.registerKey;
		var terms = data.terms;
		var serverUrl = data.serverUrl;
		var userId = data.userId;
		
		pushConnectorUrl = data.pushConnectorUrl;
		pushConnectorToken = data.pushConnectorToken;
		
		console.log("qr [" + qr + "]");
		console.log("corpId [" + corpId + "]");
		console.log("registerKey [" + registerKey + "]");
		console.log("terms [" + terms + "]");
		console.log("serverUrl [" + serverUrl + "]");
		console.log("userId [" + userId + "]");
		console.log("url [" + pushConnectorUrl + "]");
		
		$("#qr").prop("src", qr);
		//$("#qr").css("display", "block");
		
		$("#server_url").html(serverUrl);
		$("#corp_id").html(corpId);
		$("#user_id").html(userId);
		
		var today = new Date();
		autootp_millisec = today.getTime();
		autootp_terms = parseInt(terms - 1);
		
		qrSocket = null;
		drawAutoOTP();
		//regAutoOTPRepeat();
		connWebSocket();
	}
	else {
		alert("잠시후 다시 시도해 주시기 바랍니다.");
		moveHome();
	}
}

// 가입여부 확인
function regAutoOTPRepeat() {
	
	//console.log("----- regAutoOTPRepeat() -----");
	
	var today = new Date();
	var now_millisec = today.getTime();
	var gap_millisec = now_millisec - autootp_millisec;
	
	if(gap_millisec < autootp_terms * 1000) {
		
		var isReg = checkAutoOTPReg();
		console.log("isReg = " + isReg);
		
		if(isReg == "T") {
			clearTimeout(timeoutId1);
			clearTimeout(timeoutId2);
			
			alert("정상적으로 등록되었습니다.");
			
			moveHome();
		}
		else {
			timeoutId1 = setTimeout(regAutoOTPRepeat, 1500);
		}
	}
}

function drawAutoOTP() {
	var today = new Date();
	var gap_second = Math.ceil((today.getTime() - autootp_millisec) / 1000);
	
	if(gap_second < autootp_terms) {
	
		var tmp_min = parseInt((autootp_terms - gap_second) / 60);
		var tmp_sec = parseInt((autootp_terms - gap_second) % 60);
		$("#rest_time").html(tmp_min + "분 " + tmp_sec + "초");
		
		if(qrSocket != null) {
			//console.log("[" + today.getTime() + "] qrSocket state=" + qrSocket.readyState);
			if(qrSocket.readyState != qrSocket.OPEN) {
				console.log("WebSocket closed --> change [POLLING]");
				qrSocket = null;
				regAutoOTPRepeat();
			}
		}
		
		timeoutId2 = setTimeout(drawAutoOTP, 100);
	}
	else {
		clearTimeout(timeoutId1);
		clearTimeout(timeoutId2);
		
		alert("QR등록 대기시간이 만료되었습니다.");
		
		moveHome();
	}
}

function callApi(data) {

	var api_url = "/api/Login/AutoOTP/autoOTPReq";
	var ret_val = "";
	
	//console.log("---------- data -----------");
	//console.log(data);
	
	$.ajax({
		url: api_url,
		method: 'POST',
		dataType: 'json',
		data: data,
		async: false,
		success: function(data) {
			//console.log("[SUCCESS]");
			//console.log(data);
			
			ret_val = data.result;
		},
		error: function(xhr, status, error) {
			console.log("[ERROR] code: " + xhr.status + ", message: " + xhr.responseText + ", status: " + status + ", ERROR: " + error);
			$("#search_result").html("검색결과가 없습니다.");
		},
		complete: function(data) {
			//console.log("[COMPLETE]");
		}
	});
	
	return ret_val;
}

//-------------------------------------------------- WebSocket -------------------------------------------------

/*
	- WebSocket readyState
	  0 CONNECTING	소켓이 생성됐으나 연결은 아직 개방되지 않았습니다.
	  1 OPEN		연결이 개방되어 통신할 수 있습니다.
	  2 CLOSING		연결을 닫는 중입니다.
	  3 CLOSED		연결이 닫혔거나, 개방할 수 없었습니다.
*/

var qrSocket = null;
var result = null;

function connWebSocket() {

	qrSocket = new WebSocket(pushConnectorUrl);

	qrSocket.onopen = function(e) {
		console.log("######## WebSocket Connected ########");
		var send_msg = '{"pushConnectorToken":"' + pushConnectorToken + '"}';
		console.log("url [" + pushConnectorUrl + "]");
		console.log("send [" + send_msg + "]");
		qrSocket.send(send_msg);
	}

	qrSocket.onmessage = async function (event) {
		console.log("######## WebSocket Data received [" + qrSocket.readyState + "] ########");
		console.log(event);
		console.log("=================================================");
		
		try {
			if (event !== null && event !== undefined) {
				result = await JSON.parse(event.data);
				//price = result[0];
				console.log(result);
				console.log("=================================================");
			}
		} catch (err) {
			console.log(err);
		}
	}

	qrSocket.conclose = function(event) {
		if(event.wasClean)
			console.log("######## WebSocket Disconnected - OK !!! [" + qrSocket.readyState + "] ########");
		else
			console.log("######## WebSocket Disconnected - Error !!! [" + qrSocket.readyState + "] ########");

		console.log("=================================================");
		console.log(event);
		console.log("=================================================");
	}

	qrSocket.onerror = function(error) {
		console.log("######## WebSocket Error !!! [" + qrSocket.readyState + "] ########");
		console.log("=================================================");
		console.log(error);
		console.log("=================================================");
	}
}

</script>
	