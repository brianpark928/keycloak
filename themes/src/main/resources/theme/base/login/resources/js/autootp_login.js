// 자동로그인 최대 대기 시간(초)
MaxTime = 60

// --------------------------- AutoOTP --------------------------------
var autootp_millisec = 0;
var autoOtpState = "OFF";
var autootp_term = 0;
var servicePassword = "";
var pushConnectorUrl = "";
var pushConnectorToken = "";
var sessionId = "";

var timeoutId1 = null;
var timeoutId2 = null;

// --------------------------- Keycloak --------------------------------
var admin_token = "";

function loginOk() {
	var form = $("#frm");
	form.attr("method", "POST");
	form.attr("action", $("#submit_url").val());
	form.submit();
	form.empty();
}

function AutoOtpLoginRestAPI() {
	/*
	 * 1. AutoOTP 등록여부 확인
	 * 2. 일회용토큰 요청
	 * 3. 일외용토큰으로 서비스 패스워드 요청
	 * 3. 인증요청
	 * 4. 1초 주기로 인증결과 요청
	 */
	 
	var isReg = checkAutoOTPReg();
	console.log("isReg = " + isReg);
	
	if(isReg == "T") {
		var token = getTokenForOneTime();
		
		if(token != "")
			loginAutoOTPStart(token);
	}
	else {
		LoginCancel("F");
		
		console.log("미등록 유저");
		regAutoOTP();
	}
}

// 가입여부 체크
function checkAutoOTPReg() {
	console.log("----- checkAutoOTPReg() -----");
	
	var ret_val = "";

	var userId = $("#username").val();

	var data = {
		url: "isApUrl",
		params: "userId=" + userId
	}

	var result = callApi(data);
	jsonResult = JSON.parse(result.result);
	var exist = false;

	var code = jsonResult.code;
	if(code == "000" || code == "000.0")
		exist = jsonResult.data.exist;
	
	if(exist)	ret_val = "T";
	else		ret_val = "F";
	
	return ret_val;
}

// 일회용토큰 요청
function getTokenForOneTime() {
	console.log("----- getTokenForOneTime() -----");

	var ret_val = "";
	
	var userId = $("#username").val();
	var data = {
		url: "getTokenForOneTimeUrl",
		params: ""
	}
	
	var result = callApi(data);
	jsonResult = JSON.parse(result.result);
	oneTimeToken = result.oneTimeToken;
	
	var code = jsonResult.code;
	if(code == "000" || code == "000.0") {
		ret_val = oneTimeToken;
		console.log("oneTimeToken = " + ret_val);
	}

	return ret_val;
}

// 서비스 패스워드 요청
function loginAutoOTPStart(token) {
	console.log("----- loginAutoOTPStart() -----");
	
	var userId = $("#username").val();
	var data = {
		url: "getSpUrl",
		params: "userId=" + userId + "&token=" + token
	}
	
	var result = callApi(data);
	jsonResult = JSON.parse(result.result);
	
	var code = jsonResult.code;

	if(code == "000" || code == "000.0") {
		term = jsonResult.data.term;
		servicePassword = jsonResult.data.servicePassword;
		pushConnectorUrl = jsonResult.data.pushConnectorUrl;
		pushConnectorToken = jsonResult.data.pushConnectorToken;
		sessionId = result.sessionId;
		
		window.localStorage.setItem('session_id', sessionId);
		console.log("### set sessionId = " + sessionId);
		
		var today = new Date();
		autootp_millisec = today.getTime();
		autootp_term = parseInt(term - 1);
		
		console.log("term=" + term + ", servicePassword=" + servicePassword);
		
		drawAutoOTP();
		//loginAutoOTPRepeat();
		connWebSocket();
	}
	else if(code == "200.6") {
		//sessionId = result.sessionId;
		sessionId = window.localStorage.getItem('session_id');
		console.log("### get sessionId = " + sessionId);
		
		console.log("Already request authentication --> send [cancel], sessionId=" + sessionId);
		
		if(sessionId != undefined && sessionId != "" && sessionId != null) {
			var userId = $("#username").val();
			var data = {
				url: "cancelUrl",
				params: "sessionId=" + sessionId
			}
			
			var result = callApi(data);
			jsonResult = JSON.parse(result.result);
			
			var code = jsonResult.code;
		
			if(code == "000" || code == "000.0") {
				window.localStorage.removeItem('session_id');
				setTimeout(() => loginAutoOTPStart(token), 500);
			}
			else {
				alert("Please try again later.");
				moveBack();
			}
		}
		else {
			alert("Please try again later.");
			moveBack();
		}
	}
}

// 인증결과 요청
function loginAutoOTPRepeat() {
	console.log("----- loginAutoOTPRepeat() -----");
	
	var today = new Date();
	var now_millisec = today.getTime();
	var gap_millisec = now_millisec - autootp_millisec;
	
	if(gap_millisec < autootp_term * 1000 - 1000) {
		
		var userId = $("#username").val();
		var data = {
			url: "resultUrl",
			params: "sessionId=" + sessionId
		}
		
		var result = callApi(data);
		jsonResult = JSON.parse(result.result);
		
		var code = jsonResult.code;
		var auth = jsonResult.data.auth;
		
		if(code == "000" || code == "000.0") {
			
			if(auth == "Y") {
				autoOtpState = "OFF";
				clearTimeout(timeoutId1);
				clearTimeout(timeoutId2);
				
				window.localStorage.removeItem('session_id');
				
				console.log("STOP ---> AutoOTP confirmed !!!");
				
				//alert("Login Success");
				
				loginOk();
			}
			else if(auth == "N") {

				LoginCancel("F");

				autoOtpState = "OFF";
				clearTimeout(timeoutId1);
				clearTimeout(timeoutId2);
				
				window.localStorage.removeItem('session_id');
				
				console.log("STOP ---> AutoOTP canceled !!!");
				
				alert("Authentication denied.");
	
				moveBack();
			}
			else {
				timeoutId1 = setTimeout(loginAutoOTPRepeat, 1500);
			}
		}
	}
}

function drawAutoOTP() {
	var today = new Date();
	var now_millisec = today.getTime();
	var gap_millisec = now_millisec - autootp_millisec;
	
	var ratio = 100 - (gap_millisec / autootp_term / 1000) * 100 - 1;
	//console.log("ratio=" + ratio);
	
	if(ratio > 0) {
		var tmpPassword = servicePassword;
		if(tmpPassword.length == 6)
			tmpPassword = tmpPassword.substr(0, 3) + " " + tmpPassword.substr(3, 6);
		
		$("#autootp_bar").css("width", ratio + "%");
		$("#autootp_num").text(tmpPassword);
		
		if(qrSocket != null) {
			//console.log("[" + today.getTime() + "] qrSocket state=" + qrSocket.readyState);
			if(qrSocket.readyState != qrSocket.OPEN) {
				console.log("WebSocket closed --> change [POLLING]");
				qrSocket = null;
				loginAutoOTPRepeat();
			}
		}
		
		timeoutId2 = setTimeout(drawAutoOTP, 100);
	}
	else {
		autoOtpState = "OFF";
		clearTimeout(timeoutId1);
		clearTimeout(timeoutId2);
		
		CancelLogin();
	}
}

function CancelLogin() {
	LoginCancel('T');
	window.localStorage.removeItem('session_id');
	moveBack();
}

function LoginCancel(sendCancel) {
	
	console.log("----- LoginCancel [" + sendCancel + "] -----");
	
	clearTimeout(timeoutId1);
	clearTimeout(timeoutId2);
	
	autoOtpState = "OFF";
	
	$("#autootp_bar").css("width", "0%");
	$("#autootp_num").text("");
	
	if(sendCancel == "T") {
		var userId = $("#userId").val();
		var data = {
			url: "cancelUrl",
			params: "sessionId=" + sessionId
		}
		
		var result = callApi(data);
		/*
		jsonResult = JSON.parse(result.result);
		
		var code = jsonResult.code;
		if(code == "000" || code == "000.0") {
			$("#btn_text").html("로그인");
			$("#userId").prop("disabled", false);
		}
		else {
			alert("오류가 발생하였습니다.\n다시 시도하시기 바랍니다.");
			location.reload();
		}
		*/
	}
}

function callApi(data) {

	var api_url = "/auth/realms/" + $("#db_realm").val() + "/protocol/openid-connect/autootp";
	var ret_val = "";
	
	console.log("---------- data -----------");
	console.log(data);
	
	$.ajax({
		url: api_url,
		method: 'POST',
		dataType: 'json',
		data: data,
		async: false,
		success: function(data) {
			console.log("[SUCCESS]");
			console.log(data);
			
			ret_val = data;
		},
		error: function(xhr, status, error) {
			console.log("[ERROR] code: " + xhr.status + ", message: " + xhr.responseText + ", status: " + status + ", ERROR: " + error);
			$("#search_result").html("검색결과가 없습니다.");
		},
		complete: function(data) {
			console.log("[COMPLETE]");
		}
	});
	
	return ret_val;
}

function moveBack() {
	console.log(">>>>>>>>>>>>>>>>>>> moveBack");
	//history.back();
	location.href = "http://localhost/hello";
}

// -------------------------------------------------- AutoOTP 등록 -------------------------------------------------

function regAutoOTP() {
	$("#autoOtpLogin").css("display", "none");
	$("#reg_qr").css("display", "block");
	
	var username = $("#hidden_username").val();
	
	var data = {
		url: "joinApUrl",
		params: "userId=" + username
	}
	
	var result = callApi(data);
	console.log(result);
	jsonResult = JSON.parse(result.result);
	
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
		
		//$("#server_url").html(serverUrl);
		//$("#corp_id").html(corpId);
		$("#user_id").html(userId);
		
		var today = new Date();
		autootp_millisec = today.getTime();
		autootp_terms = parseInt(terms - 1);
		
		qrSocket = null;
		drawAutoOTPReg();
		//regAutoOTPRepeat();
		connWebSocket();
	}
	else {
		alert("Please try again later.");
		
		moveBack();
	}
}

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
			
			alert("Registration Completed");
			
			moveBack();
		}
		else {
			timeoutId1 = setTimeout(regAutoOTPRepeat, 1500);
		}
	}
}

function drawAutoOTPReg() {
	var today = new Date();
	var gap_second = Math.ceil((today.getTime() - autootp_millisec) / 1000);
	
	if(gap_second < autootp_terms) {
	
		var tmp_min = parseInt((autootp_terms - gap_second) / 60);
		var tmp_sec = parseInt((autootp_terms - gap_second) % 60);
		
		if(tmp_min == 0 && tmp_sec == 1)
			tmp_sec = "00";
		else if(tmp_sec < 10)
			tmp_sec = "0" + tmp_sec;
			
		$("#rest_time").html(tmp_min + " : " + tmp_sec);
		
		if(qrSocket != null) {
			//console.log("[" + today.getTime() + "] qrSocket state=" + qrSocket.readyState);
			if(qrSocket.readyState != qrSocket.OPEN) {
				console.log("WebSocket closed --> change [POLLING]");
				qrSocket = null;
				regAutoOTPRepeat();
			}
		}
		
		timeoutId2 = setTimeout(drawAutoOTPReg, 100);
	}
	else {
		clearTimeout(timeoutId1);
		clearTimeout(timeoutId2);
		
		alert("Registration Time Out");
		
		moveBack();
	}
}

// -------------------------------------------------- AutoOTP 해지 -------------------------------------------------

function loginAutoOTPwithdrawal() {
	console.log("----- loginAutoOTPwithdrawal() -----");
	
	LoginCancel('T');
	
	if(confirm("Do you really want to Unregistrate AutoOTP?")) {
		
		var username = $("#hidden_username").val();
		
		var data = {
			url: "withdrawalApUrl",
			params: "userId=" + username
		}
		
		var result = callApi(data);
		console.log(result);
		jsonResult = JSON.parse(result.result);
		
		var code = jsonResult.code;
		if(code == "000" || code == "000.0") {
			alert("Unregistrated OK");
			moveBack();
		}
		else {
			alert("Try again");
			moveBack();
		}
	}
	else {
		AutoOtpLoginRestAPI();
	}
}


// -------------------------------------------------- WebSocket -------------------------------------------------

/*
	- WebSocket readyState
	  0 CONNECTING	소켓이 생성됐으나 연결은 아직 개방되지 않았습니다.
	  1 OPEN		연결이 개방되어 통신할 수 있습니다.
	  2 CLOSING		연결을 닫는 중입니다.
	  3 CLOSED		연결이 닫혔거나, 개방할 수 없었습니다.
*/

var qrSocket = null;

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
