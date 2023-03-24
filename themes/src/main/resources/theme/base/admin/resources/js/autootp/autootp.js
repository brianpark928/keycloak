
module.controller('AutoOTPCtrl', function($scope, $http)
{
//	if($scope.realm.attributes.autootpAppSettingStep != undefined && $scope.realm.attributes.autootpAppSettingStep.length > 0) { 
	if($scope.realm.attributes.autootpReturnDomainValidationToken != undefined && $scope.realm.attributes.autootpReturnDomainValidationToken.length > 0) { 
	//	$scope.autootpAppSettingStepDisable = true;
		$scope.autootpAppSettingNameDisable = true;
		$scope.autootpAppSettingDomainDisable = true;
		$scope.autootpAppSettingIpAddressDisable = true;
		$scope.autootpAppSettingProxyServerDomainDisable = true;
		$scope.autootpAppSettingEmailDisable = true;
		
		$scope.autootpAppSettingDeleteDisable = false;
	} else { 
		$scope.autootpAppSettingStepDisable = false;
		$scope.autootpAppSettingNameDisable = false;
		$scope.autootpAppSettingDomainDisable = false;
		$scope.autootpAppSettingIpAddressDisable = false;
		$scope.autootpAppSettingProxyServerDomainDisable = false;
		$scope.autootpAppSettingEmailDisable = false;
		
		$scope.autootpAppSettingDeleteDisable = true;
	}  

    $scope.autootpAppSettingSaveDisable = true;

	if($scope.realm.attributes.autootpReturnDomainValidationToken != undefined && $scope.realm.attributes.autootpReturnDomainValidationToken.length > 0) { 
    	$scope.autootpDevcenterReloadDisable = false;
	} else { 
		$scope.autootpDevcenterReloadDisable = true;
	}  

	if($scope.realm.attributes.autootpReturnServerProgressStatus != undefined && $scope.realm.attributes.autootpReturnServerProgressStatus.length > 0) {
		switch($scope.realm.attributes.autootpReturnServerProgressStatus){
			case "01" :
		        $scope.autootpDevcenterRemailDisable = false;
				$scope.autootpDevcenterRemailSettingDisable = true;
				break;
			
			case "02" :
		        $scope.autootpDevcenterRemailDisable = true;
				$scope.autootpDevcenterRemailSettingDisable = true;
				break;

			case "10" :
		        $scope.autootpDevcenterRemailDisable = true;
				$scope.autootpDevcenterRemailSettingDisable = false;
				break;
			default :
		        $scope.autootpDevcenterRemailDisable = true;
				$scope.autootpDevcenterRemailSettingDisable = true;
				break;
							
		} 

	} else {
        $scope.autootpDevcenterRemailDisable = true;
		$scope.autootpDevcenterRemailSettingDisable = true;
		
	}


    $scope.autootpServerSettingSaveDisable = true;
    
	if(($scope.realm.attributes.autootpServerSettingAppServerKey != undefined && $scope.realm.attributes.autootpServerSettingAppServerKey.length > 0) ||
	   ($scope.realm.attributes.autootpServerSettingAuthServerDomain != undefined && $scope.realm.attributes.autootpServerSettingAuthServerDomain.length > 0)) { 
    	$scope.autootpServerSettingClearDisable = false;
    }else{
    	$scope.autootpServerSettingClearDisable = true;
	}

});

module.directive('kcAutootpStepSave', function ($compile, $timeout, Notifications, $http) {
    var clickDelay = 500; // 500 ms

    return {
        restrict: 'A',
        link: function ($scope, elem, attr, ctrl) {
            elem.addClass("btn btn-primary");
            elem.attr("type","submit");

            var disabled = false;
            elem.on('change', function(evt) {
                if ($scope.hasOwnProperty("changed") && !$scope.changed) return;

                // KEYCLOAK-4121: Prevent double form submission
                if (disabled) {
                    evt.preventDefault();
                    evt.stopImmediatePropagation();
                    return;
                } else {
                    disabled = true;
                    $timeout(function () { disabled = false; }, clickDelay, false);
                }

				if($scope.realm.attributes.autootpReturnDomainValidationToken == undefined || $scope.realm.attributes.autootpReturnDomainValidationToken.length == 0) { 
			    	return;
				}

                $scope.$apply(function() {
                    var form = elem.closest('form');
                    if (form && form.attr('name')) {
                        var ngValid = form.find('.ng-valid');
                        if ($scope[form.attr('name')].$valid) {
                            //ngValid.removeClass('error');
                            ngValid.parent().removeClass('has-error');
							
							$scope['autootpsave']();
                            Notifications.success("AutoOTP authentication step change complete.")
                            
                        } else {
                            Notifications.error("Missing or invalid field(s). Please verify the fields in red.")
                            //ngValid.removeClass('error');
                            ngValid.parent().removeClass('has-error');

                            var ngInvalid = form.find('.ng-invalid');
                            //ngInvalid.addClass('error');
                            ngInvalid.parent().addClass('has-error');
                        }
                    }
                });
            })
        }
    }
});

module.directive('kcAutootpAppSave', function ($compile, $timeout, Notifications, $http) {
    var clickDelay = 500; // 500 ms

    return {
        restrict: 'A',
        link: function ($scope, elem, attr, ctrl) {
            elem.addClass("btn btn-primary");
            elem.attr("type","submit");

            var disabled = false;
            elem.on('click', function(evt) {
                if ($scope.hasOwnProperty("changed") && !$scope.changed) return;

                // KEYCLOAK-4121: Prevent double form submission
                if (disabled) {
                    evt.preventDefault();
                    evt.stopImmediatePropagation();
                    return;
                } else {
                    disabled = true;
                    $timeout(function () { disabled = false; }, clickDelay, false);
                }

                $scope.$apply(function() {
                    var form = elem.closest('form');
                    if (form && form.attr('name')) {
                        var ngValid = form.find('.ng-valid');
                        if ($scope[form.attr('name')].$valid) {
                            //ngValid.removeClass('error');
//                            ngValid.parent().removeClass('has-error');

							var paramStr = "/auth/realms/master/protocol/openid-connect/autootp-policy-api"; // master로 저장됨
							
							//var paramStr = "/auth/realms/DevRealm/protocol/openid-connect/autootp-policy-api"; // 저장안되고 session에만 단발성으로 남아있음
							paramStr = paramStr + "?urlKey=kcAutootpAppSave";
							paramStr = paramStr + "&appName="+$scope.realm.attributes.autootpAppSettingName;
							paramStr = paramStr + "&appDomain="+$scope.realm.attributes.autootpAppSettingDomain;
							paramStr = paramStr + "&appIp="+$scope.realm.attributes.autootpAppSettingIpAddress;
							paramStr = paramStr + "&authDomain="+$scope.realm.attributes.autootpAppSettingProxyServerDomain;
							paramStr = paramStr + "&mail="+$scope.realm.attributes.autootpAppSettingEmail;
//							paramStr = paramStr + "&pubKey="+$scope.realm.attributes.autootpAppSettingPublickey;
//							paramStr = paramStr + "&appID="+"applicationID";

							$http.get(paramStr)
	                        .then(function (response) {
								console.log(response);
								result = response.data.result;
								const objSave = JSON.parse(result);

								if(objSave.code == undefined) { 
									Notifications.error("Data delete key request error ["+objSave.code+"]"); 
								} else {
									if(objSave.code == "000.0") {

								    $scope.autootpAppSettingStepDisable = false;
								    
		                            $scope.autootpAppSettingNameDisable = true;
		                            $scope.autootpAppSettingDomainDisable = true;
		                            $scope.autootpAppSettingIpAddressDisable = true;
		                            $scope.autootpAppSettingProxyServerDomainDisable = true;
		                            $scope.autootpAppSettingEmailDisable = true;
		
		                            $scope.autootpAppSettingSaveDisable = true;
		                            $scope.autootpAppSettingDeleteDisable = false;
		                            
		                            $scope.autootpDevcenterReloadDisable = false;
							    	$scope.autootpDevcenterRemailDisable = true;
							    	$scope.autootpDevcenterRemailSettingDisable = true;

									if(objSave.data.appID == undefined) {
										$scope.realm.attributes.autootpAppSettingappID = "Server data error.[1]"; 
										Notifications.error("Data save Error~!");
										return;
									}
									else {$scope.realm.attributes.autootpAppSettingappID = objSave.data.appID;}
									
									if(objSave.data.dnsTxt == undefined) {
										$scope.realm.attributes.autootpReturnDomainValidationToken = "Server data error.[2]"; 
										Notifications.error("Data save Error~!");
										return;
									}
									else {$scope.realm.attributes.autootpReturnDomainValidationToken = objSave.data.dnsTxt;}
		                            
		                            $scope.realm.attributes.autootpReturnServerProgress = "Registration information review in progress...";
		                        	
		                        	$scope['autootpsave']();
	                            
		                            Notifications.success("Data save success.");
		                		
								} else if(objSave.code == "000.1") {	Notifications.error("Unknown Server error");
								} else if(objSave.code == "000.2") {	Notifications.error("Parameter error");
								} else if(objSave.code == "100.1") {	Notifications.error("Mail sending error");
								} else if(objSave.code == "100.2") {	Notifications.error("Duplicate application domain");
								} else if(objSave.code == "100.3") {	Notifications.error("Duplicate application name");
								} else if(objSave.code == "100.4") {	Notifications.error("Duplicate proxy server domain");
								} else if(objSave.code == "100.5") {	Notifications.error("Email unavailable");
								} else {								Notifications.error("Exception error"); 
								}
								
							} 
								
								
	                        }, function(error) {
							    console.log(error);
							    Notifications.error('Data save Error~!');
							});

                        } else {
                            Notifications.error("Missing or invalid field(s). Please verify the fields in red.")
                            //ngValid.removeClass('error');
                            ngValid.parent().removeClass('has-error');

                            var ngInvalid = form.find('.ng-invalid');
                            //ngInvalid.addClass('error');
                            ngInvalid.parent().addClass('has-error');
                        }
                    }
                });
            })
        }
    }
});
		
module.directive('kcAutootpDelete', function ($compile, $timeout, Notifications, $http) {
    var clickDelay = 500; // 500 ms

    return {
        restrict: 'A',
        link: function ($scope, elem, attr, ctrl) {
            elem.addClass("btn btn-danger");
            elem.attr("type","submit");
            
            var disabled = false;
            elem.on('click', function(evt) {
				if(!confirm("All registered contents will be deleted.\nAre you sure you want to delete it?")){
					return;
				}else{
	                if ($scope.hasOwnProperty("changed") && !$scope.changed) return;
	
	                // KEYCLOAK-4121: Prevent double form submission
	                if (disabled) {
	                    evt.preventDefault();
	                    evt.stopImmediatePropagation();
	                    return;
	                } else {
	                    disabled = true;
	                    $timeout(function () { disabled = false; }, clickDelay, false);
	                }

	                $scope.$apply(function() {
	                    var form = elem.closest('form');
	                    if (form && form.attr('name')) {
	                        var ngValid = form.find('.ng-valid');
	                        if ($scope[form.attr('name')].$valid) {
	                            //ngValid.removeClass('error');
	//                            ngValid.parent().removeClass('has-error');
	                            
	

							var paramStr = "/auth/realms/master/protocol/openid-connect/autootp-policy-api"; // master로 저장됨
							var delkey = "";
							
							paramStr = paramStr + "?urlKey=kcAutootpDeleteKey";
							paramStr = paramStr + "&appID="+$scope.realm.attributes.autootpAppSettingappID;

							var responseCode = "";
							
							$http.get(paramStr)
	                        .then(function (response) {
								console.log(response);
								result = response.data.result;
								const objKey = JSON.parse(result);
								
								if(objKey.code == undefined) { 
									responseCode = "001";
									Notifications.error("Data delete key request error [1]"); 
								} else if(objKey.code == "000.0") {
									if(objKey.data.delkey == undefined) {
										responseCode = "002";
										$scope.realm.attributes.autootpReturnServerProgress = "Data delete key request error [2]";
									} else {
										delkey = objKey.data.delkey;
										/////////////////////////// kcAutootpDeleteKey http success process
										
										paramStr = "/auth/realms/master/protocol/openid-connect/autootp-policy-api"; // master로 저장됨
										
										paramStr = paramStr + "?urlKey=kcAutootpDelete";
										paramStr = paramStr + "&delkey="+delkey;
										paramStr = paramStr + "&appID="+$scope.realm.attributes.autootpAppSettingappID;
			
										$http.get(paramStr)
				                        .then(function (response) {
											console.log(response);
											result = response.data.result;
											const objDel = JSON.parse(result);

											if(objDel.code == undefined) { 
												responseCode = "003";
												Notifications.error("Data delete key request error [3]"); 
											} else {
												if(objDel.code == "000.0" ) {
			
				                            
													responseCode = "000";
													/////////////////////////// kcAutootpDelete http success process
													
						                            $scope.realm.attributes.autootpAppSettingStep = "";
						                            $scope.realm.attributes.autootpAppSettingName = "";
						                            $scope.realm.attributes.autootpAppSettingDomain = "";
						                            $scope.realm.attributes.autootpAppSettingIpAddress = "";
						                            $scope.realm.attributes.autootpAppSettingProxyServerDomain = "";
						                            $scope.realm.attributes.autootpAppSettingEmail = "";
						
						                            $scope.realm.attributes.autootpReturnDomainValidationToken = "";
						                            $scope.realm.attributes.autootpReturnServerProgress = "";
													$scope.realm.attributes.autootpReturnServerProgressStatus = "";
						
						                            $scope.realm.attributes.autootpServerSettingAppServerKey = "";
						                            $scope.realm.attributes.autootpServerSettingAuthServerDomain = "";
						
						                            $scope['autootpdelete']();
						                            
						                            $scope.autootpAppSettingStepDisable = false;
						                            $scope.autootpAppSettingNameDisable = false;
						                            $scope.autootpAppSettingDomainDisable = false;
						                            $scope.autootpAppSettingIpAddressDisable = false;
						                            $scope.autootpAppSettingProxyServerDomainDisable = false;
						                            $scope.autootpAppSettingEmailDisable = false;
						                            
												    $scope.autootpAppSettingSaveDisable = true;
												    $scope.autootpAppSettingDeleteDisable = true;
												
												    $scope.autootpDevcenterReloadDisable = true;
											    	$scope.autootpDevcenterRemailDisable = true;
											    	$scope.autootpDevcenterRemailSettingDisable = true;

												    $scope.autootpServerSettingSaveDisable = true;
												    $scope.autootpServerSettingClearDisable = true;
																		
													/////////////////////////// kcAutootpDelete http success process
						                            Notifications.success("Server Data Delete completed.");
					                		
												} else if(objDel.code == "000.1") {	Notifications.error("Unknown Server error");
												} else if(objDel.code == "000.2") {	Notifications.error("Parameter error");
												} else if(objDel.code == "100.1") {	Notifications.error("Mail sending error");
												} else if(objDel.code == "100.2") {	Notifications.error("Duplicate application domain");
												} else if(objDel.code == "100.3") {	Notifications.error("Duplicate application name");
												} else if(objDel.code == "100.4") {	Notifications.error("Duplicate proxy server domain");
												} else if(objDel.code == "100.5") {	Notifications.error("Email unavailable");
												} else if(objDel.code == "100.6") {	

													responseCode = "000";
													/////////////////////////// kcAutootpDelete http success process
													
						                            $scope.realm.attributes.autootpAppSettingStep = "";
						                            $scope.realm.attributes.autootpAppSettingName = "";
						                            $scope.realm.attributes.autootpAppSettingDomain = "";
						                            $scope.realm.attributes.autootpAppSettingIpAddress = "";
						                            $scope.realm.attributes.autootpAppSettingProxyServerDomain = "";
						                            $scope.realm.attributes.autootpAppSettingEmail = "";
						
						                            $scope.realm.attributes.autootpReturnDomainValidationToken = "";
						                            $scope.realm.attributes.autootpReturnServerProgress = "";
													$scope.realm.attributes.autootpReturnServerProgressStatus = "";
						
						                            $scope.realm.attributes.autootpServerSettingAppServerKey = "";
						                            $scope.realm.attributes.autootpServerSettingAuthServerDomain = "";
						
						                            $scope['autootpdelete']();
						                            
						                            $scope.autootpAppSettingStepDisable = false;
						                            $scope.autootpAppSettingNameDisable = false;
						                            $scope.autootpAppSettingDomainDisable = false;
						                            $scope.autootpAppSettingIpAddressDisable = false;
						                            $scope.autootpAppSettingProxyServerDomainDisable = false;
						                            $scope.autootpAppSettingEmailDisable = false;
						                            
												    $scope.autootpAppSettingSaveDisable = true;
												    $scope.autootpAppSettingDeleteDisable = true;
												
												    $scope.autootpDevcenterReloadDisable = true;
													$scope.autootpDevcenterRemailDisable = true;
													$scope.autootpDevcenterRemailSettingDisable = true;

												    $scope.autootpServerSettingSaveDisable = true;
												    $scope.autootpServerSettingClearDisable = true;
																		
													/////////////////////////// kcAutootpDelete http success process
						                            Notifications.success("Reset data. Server Data Delete completed.");

												} else {							
													Notifications.error("Exception error"); 
												}
												
											} 
												
												

					                        }, function(error) {
											    console.log(error);
											    Notifications.error('Server Data Delete Error~!');
											});										
										/////////////////////////// kcAutootpDeleteKey http success process
										
										}
									} else if(objKey.code == "100.6") {			
										
										responseCode = "000";
										/////////////////////////// kcAutootpDelete http success process
										
			                            $scope.realm.attributes.autootpAppSettingStep = "";
			                            $scope.realm.attributes.autootpAppSettingName = "";
			                            $scope.realm.attributes.autootpAppSettingDomain = "";
			                            $scope.realm.attributes.autootpAppSettingIpAddress = "";
			                            $scope.realm.attributes.autootpAppSettingProxyServerDomain = "";
			                            $scope.realm.attributes.autootpAppSettingEmail = "";
			
			                            $scope.realm.attributes.autootpReturnDomainValidationToken = "";
			                            $scope.realm.attributes.autootpReturnServerProgress = "";
										$scope.realm.attributes.autootpReturnServerProgressStatus = "";
										
			                            $scope.realm.attributes.autootpServerSettingAppServerKey = "";
			                            $scope.realm.attributes.autootpServerSettingAuthServerDomain = "";
			
			                            $scope['autootpdelete']();
			                            
			                            $scope.autootpAppSettingStepDisable = false;
			                            $scope.autootpAppSettingNameDisable = false;
			                            $scope.autootpAppSettingDomainDisable = false;
			                            $scope.autootpAppSettingIpAddressDisable = false;
			                            $scope.autootpAppSettingProxyServerDomainDisable = false;
			                            $scope.autootpAppSettingEmailDisable = false;
			                            
									    $scope.autootpAppSettingSaveDisable = true;
									    $scope.autootpAppSettingDeleteDisable = true;
									
									    $scope.autootpDevcenterReloadDisable = true;
								    	$scope.autootpDevcenterRemailDisable = true;
								    	$scope.autootpDevcenterRemailSettingDisable = true;

									    $scope.autootpServerSettingSaveDisable = true;
									    $scope.autootpServerSettingClearDisable = true;
															
										/////////////////////////// kcAutootpDelete http success process
			                            Notifications.success("Reset data. Server Data Delete completed.");
								
									} else {
										//$scope.realm.attributes.autootpReturnServerProgress = "Server Data Delete Error~!";
										Notifications.error("Server Data Delete Error~!");
									}
								

		                            //Notifications.success("Server Data Delete completed.")
		                        }, function(error) {
								    console.log(error);
								    Notifications.error('Server Data Delete Error~!');
								});


	                        } else {
	                            Notifications.error("Missing or invalid field(s). Please verify the fields in red.")
	                            //ngValid.removeClass('error');
	                            ngValid.parent().removeClass('has-error');
	
	                            var ngInvalid = form.find('.ng-invalid');
	                            //ngInvalid.addClass('error');
	                            ngInvalid.parent().addClass('has-error');
	                        }
	                    }
	                });
	                
                }
            })            
            
        }
    }
});


module.directive('kcDevcenterReload', function ($compile, $timeout, Notifications, $http) {
    var clickDelay = 500; // 500 ms

    return {
        restrict: 'A',
        link: function ($scope, elem, attr, ctrl) {
            elem.addClass("btn btn-primary");
            elem.attr("type","submit");

            var disabled = false;
            elem.on('click', function(evt) {
                if ($scope.hasOwnProperty("changed") && !$scope.changed) return;

                // KEYCLOAK-4121: Prevent double form submission
                if (disabled) {
                    evt.preventDefault();
                    evt.stopImmediatePropagation();
                    return;
                } else {
                    disabled = true;
                    $timeout(function () { disabled = false; }, clickDelay, false);
                }

                $scope.$apply(function() {
                    var form = elem.closest('form');
                    if (form && form.attr('name')) {
                        var ngValid = form.find('.ng-valid');
                        if ($scope[form.attr('name')].$valid) {
                            //ngValid.removeClass('error');
//                            ngValid.parent().removeClass('has-error');
                            

							var paramStr = "/auth/realms/master/protocol/openid-connect/autootp-policy-api"; // master로 저장됨
							
							paramStr = paramStr + "?urlKey=kcDevcenterReload";
							paramStr = paramStr + "&appID="+$scope.realm.attributes.autootpAppSettingappID;

							$http.get(paramStr)
	                        .then(function (response) {
								console.log(response);
								result = response.data.result;
								const objReload = JSON.parse(result);
								
								
	                            //$scope.autootpAppSettingStepDisable = true;
	                            $scope.autootpAppSettingSaveDisable = true;
	                            $scope.autootpAppSettingDeleteDisable = false;
	                            
	                            if(objReload.code == undefined){
										$scope.realm.attributes.autootpReturnServerProgress = "Server progress Reload error.[1]"; 
										Notifications.error("Server progress Reload error~!");
										return;
								}else if(objReload.code == "000.0") {
									if(objReload.data.status == undefined) {
										$scope.realm.attributes.autootpReturnServerProgress = "Server progress Reload error.[1]"; 
										Notifications.error("Server progress Reload error~!");
										return;
									}
									else {
										switch(objReload.data.status){
											case "01" :
												$scope.realm.attributes.autootpReturnServerProgress = "Validating mail...";
												$scope.realm.attributes.autootpReturnServerProgressStatus = objReload.data.status;
					                            $scope.autootpDevcenterRemailDisable = false;
												$scope.autootpDevcenterRemailSettingDisable = true;
												break;
											case "02" :
												$scope.realm.attributes.autootpReturnServerProgress = "Validating registration domain...";
												$scope.realm.attributes.autootpReturnServerProgressStatus = objReload.data.status;
					                            $scope.autootpDevcenterRemailDisable = true;
												$scope.autootpDevcenterRemailSettingDisable = true;
												break;
											case "10" :
												$scope.realm.attributes.autootpReturnServerProgress = "AutoOTP service is running ...";
												$scope.realm.attributes.autootpReturnServerProgressStatus = objReload.data.status;
					                            $scope.autootpDevcenterRemailDisable = true;
												$scope.autootpDevcenterRemailSettingDisable = false;
												$scope.autootpServerSettingSaveDisable = false;
												$scope.autootpServerSettingClearDisable = false;
												break;
											case "11" :
												$scope.realm.attributes.autootpReturnServerProgress = "Validating deleted email...";
												break;
											default :
												$scope.realm.attributes.autootpReturnServerProgress = "Exception status ["+objReload.data.status+"]";
												break;
										}
										Notifications.success("Server progress Reload completed.");
									}
									$scope['autootpsave']();
	                            	//Notifications.success("Server progress Reload completed.");
	                            	
								} else if(objReload.code == "000.1") {	Notifications.error("Unknown Server error");
								} else if(objReload.code == "000.2") {	Notifications.error("Parameter error");
								} else if(objReload.code == "100.1") {	Notifications.error("Mail sending error");
								} else if(objReload.code == "100.2") {	Notifications.error("Duplicate application domain");
								} else if(objReload.code == "100.3") {	Notifications.error("Duplicate application name");
								} else if(objReload.code == "100.4") {	Notifications.error("Duplicate proxy server domain");
								} else if(objReload.code == "100.5") {	Notifications.error("Email unavailable");
								} else if(objReload.code == "100.6") {
									alert("등록 후 3일이 경과되어 서버에서 자동 삭제처리 됩니다.\n새로 등록 해 주시기 바랍니다.");
									/////////////////////////// kcAutootpDelete http success process
													
		                            $scope.realm.attributes.autootpAppSettingStep = "";
		                            $scope.realm.attributes.autootpAppSettingName = "";
		                            $scope.realm.attributes.autootpAppSettingDomain = "";
		                            $scope.realm.attributes.autootpAppSettingIpAddress = "";
		                            $scope.realm.attributes.autootpAppSettingProxyServerDomain = "";
		                            $scope.realm.attributes.autootpAppSettingEmail = "";
		
		                            $scope.realm.attributes.autootpReturnDomainValidationToken = "";
		                            $scope.realm.attributes.autootpReturnServerProgress = "";
		
		                            $scope.realm.attributes.autootpServerSettingAppServerKey = "";
		                            $scope.realm.attributes.autootpServerSettingAuthServerDomain = "";
		
		                            $scope['autootpdelete']();
		                            
		                            $scope.autootpAppSettingStepDisable = false;
		                            $scope.autootpAppSettingNameDisable = false;
		                            $scope.autootpAppSettingDomainDisable = false;
		                            $scope.autootpAppSettingIpAddressDisable = false;
		                            $scope.autootpAppSettingProxyServerDomainDisable = false;
		                            $scope.autootpAppSettingEmailDisable = false;
		                            
								    $scope.autootpAppSettingSaveDisable = true;
								    $scope.autootpAppSettingDeleteDisable = true;
								
								    $scope.autootpDevcenterReloadDisable = true;
							    	$scope.autootpDevcenterRemailDisable = true;
							    	$scope.autootpDevcenterRemailSettingDisable = true;

								    $scope.autootpServerSettingSaveDisable = true;
								    $scope.autootpServerSettingClearDisable = true;
														
									/////////////////////////// kcAutootpDelete http success process
									Notifications.success("Reset data. Server Data Delete completed.");

								} else {
									$scope.realm.attributes.autootpReturnServerProgress = "Server progress Reload error.";
									Notifications.error("Server progress Reload error.");
								}
								
	                            

	                           
	                        }, function(error) {
							    console.log(error);
							    Notifications.error("Server progress Reload error.");
							});



                        } else {
                            Notifications.error("Missing or invalid field(s). Please verify the fields in red.")
                            //ngValid.removeClass('error');
                            ngValid.parent().removeClass('has-error');

                            var ngInvalid = form.find('.ng-invalid');
                            //ngInvalid.addClass('error');
                            ngInvalid.parent().addClass('has-error');
                        }
                    }
                });
            })
        }
    }
});



module.directive('kcDevcenterRemail', function ($compile, $timeout, Notifications, $http) {
    var clickDelay = 500; // 500 ms

    return {
        restrict: 'A',
        link: function ($scope, elem, attr, ctrl) {
            elem.addClass("btn btn-primary");
            elem.attr("type","submit");

            var disabled = false;
            elem.on('click', function(evt) {
                if ($scope.hasOwnProperty("changed") && !$scope.changed) return;

                // KEYCLOAK-4121: Prevent double form submission
                if (disabled) {
                    evt.preventDefault();
                    evt.stopImmediatePropagation();
                    return;
                } else {
                    disabled = true;
                    $timeout(function () { disabled = false; }, clickDelay, false);
                }

                $scope.$apply(function() {
                    var form = elem.closest('form');
                    if (form && form.attr('name')) {
                        var ngValid = form.find('.ng-valid');
                        if ($scope[form.attr('name')].$valid) {
                            //ngValid.removeClass('error');
//                            ngValid.parent().removeClass('has-error');
                            

							var paramStr = "/auth/realms/master/protocol/openid-connect/autootp-policy-api"; // master로 저장됨
							
							paramStr = paramStr + "?urlKey=kcDevcenterRemail";
							paramStr = paramStr + "&appID="+$scope.realm.attributes.autootpAppSettingappID;

							$http.get(paramStr)
	                        .then(function (response) {
								console.log(response);
								result = response.data.result;
								const objRemail = JSON.parse(result);
								
								
	                            if(objRemail.code == undefined) {
										$scope.realm.attributes.autootpReturnServerProgress = "Email Resend Request error.[1]"; 
										Notifications.error("Email Resend Request error~!");
										return;
								}else if(objRemail.code == "000.0") {
									Notifications.success("Request to resend verification mail completed.");
								} else if(objRemail.code == "100.1") {
										$scope.realm.attributes.autootpReturnServerProgress = "Email Resend Request error.[2]"; 
										Notifications.error("Email Resend Request error~!");
										return;
								} else if(objRemail.code == "100.6") {
									alert("등록 후 3일이 경과되어 서버에서 자동 삭제처리 됩니다.\n새로 등록 해 주시기 바랍니다.");
									/////////////////////////// kcAutootpDelete http success process
													
		                            $scope.realm.attributes.autootpAppSettingStep = "";
		                            $scope.realm.attributes.autootpAppSettingName = "";
		                            $scope.realm.attributes.autootpAppSettingDomain = "";
		                            $scope.realm.attributes.autootpAppSettingIpAddress = "";
		                            $scope.realm.attributes.autootpAppSettingProxyServerDomain = "";
		                            $scope.realm.attributes.autootpAppSettingEmail = "";
		
		                            $scope.realm.attributes.autootpReturnDomainValidationToken = "";
		                            $scope.realm.attributes.autootpReturnServerProgress = "";
		
		                            $scope.realm.attributes.autootpServerSettingAppServerKey = "";
		                            $scope.realm.attributes.autootpServerSettingAuthServerDomain = "";
		
		                            $scope['autootpdelete']();
		                            
		                            $scope.autootpAppSettingStepDisable = false;
		                            $scope.autootpAppSettingNameDisable = false;
		                            $scope.autootpAppSettingDomainDisable = false;
		                            $scope.autootpAppSettingIpAddressDisable = false;
		                            $scope.autootpAppSettingProxyServerDomainDisable = false;
		                            $scope.autootpAppSettingEmailDisable = false;
		                            
								    $scope.autootpAppSettingSaveDisable = true;
								    $scope.autootpAppSettingDeleteDisable = true;
								
								    $scope.autootpDevcenterReloadDisable = true;
							    	$scope.autootpDevcenterRemailDisable = true;
							    	$scope.autootpDevcenterRemailSettingDisable = true;

								    $scope.autootpServerSettingSaveDisable = true;
								    $scope.autootpServerSettingClearDisable = true;
														
									/////////////////////////// kcAutootpDelete http success process
									Notifications.success("AutoOTP Data Delete completed.");
								} else {
										$scope.realm.attributes.autootpReturnServerProgress = "Email Resend Request error.[3]"; 
										Notifications.error("Email Resend Request error~!");
										return;
								}
								
	                        }, function(error) {
							    console.log(error);
							    $scope.realm.attributes.autootpReturnServerProgress = "Email Resend Request error.[4]"; 
								Notifications.error("Email Resend Request error~!");
							});



                        } else {
                            Notifications.error("Missing or invalid field(s). Please verify the fields in red.")
                            //ngValid.removeClass('error');
                            ngValid.parent().removeClass('has-error');

                            var ngInvalid = form.find('.ng-invalid');
                            //ngInvalid.addClass('error');
                            ngInvalid.parent().addClass('has-error');
                        }
                    }
                });
            })
        }
    }
});





module.directive('kcDevcenterRemailSetting', function ($compile, $timeout, Notifications, $http) {
    var clickDelay = 500; // 500 ms

    return {
        restrict: 'A',
        link: function ($scope, elem, attr, ctrl) {
            elem.addClass("btn btn-primary");
            elem.attr("type","submit");

            var disabled = false;
            elem.on('click', function(evt) {
                if ($scope.hasOwnProperty("changed") && !$scope.changed) return;

                // KEYCLOAK-4121: Prevent double form submission
                if (disabled) {
                    evt.preventDefault();
                    evt.stopImmediatePropagation();
                    return;
                } else {
                    disabled = true;
                    $timeout(function () { disabled = false; }, clickDelay, false);
                }

                $scope.$apply(function() {
                    var form = elem.closest('form');
                    if (form && form.attr('name')) {
                        var ngValid = form.find('.ng-valid');
                        if ($scope[form.attr('name')].$valid) {
                            //ngValid.removeClass('error');
//                            ngValid.parent().removeClass('has-error');
                            

							var paramStr = "/auth/realms/master/protocol/openid-connect/autootp-policy-api"; // master로 저장됨
							
							paramStr = paramStr + "?urlKey=kcDevcenterRemailSetting";
							paramStr = paramStr + "&appID="+$scope.realm.attributes.autootpAppSettingappID;

							$http.get(paramStr)
	                        .then(function (response) {
								console.log(response);
								result = response.data.result;
								const objRemailSetting = JSON.parse(result);
								
								
	                            if(objRemailSetting.code == undefined) {
										$scope.realm.attributes.autootpReturnServerProgress = "Setting email Resend Request error.[1]"; 
										Notifications.error("Email Resend Request error~!");
										return;
								}else if(objRemailSetting.code == "000.0") {
									Notifications.success("Request to resend Setting mail completed.");
								} else if(objRemailSetting.code == "100.1") {
										$scope.realm.attributes.autootpReturnServerProgress = "Setting email Resend Request error.[2]"; 
										Notifications.error("Setting email Resend Request error~!");
										return;
								} else if(objRemailSetting.code == "100.6") {
									alert("등록 후 3일이 경과되어 서버에서 자동 삭제처리 됩니다.\n새로 등록 해 주시기 바랍니다.");
									/////////////////////////// kcAutootpDelete http success process
													
		                            $scope.realm.attributes.autootpAppSettingStep = "";
		                            $scope.realm.attributes.autootpAppSettingName = "";
		                            $scope.realm.attributes.autootpAppSettingDomain = "";
		                            $scope.realm.attributes.autootpAppSettingIpAddress = "";
		                            $scope.realm.attributes.autootpAppSettingProxyServerDomain = "";
		                            $scope.realm.attributes.autootpAppSettingEmail = "";
		
		                            $scope.realm.attributes.autootpReturnDomainValidationToken = "";
		                            $scope.realm.attributes.autootpReturnServerProgress = "";
		
		                            $scope.realm.attributes.autootpServerSettingAppServerKey = "";
		                            $scope.realm.attributes.autootpServerSettingAuthServerDomain = "";
		
		                            $scope['autootpdelete']();
		                            
		                            $scope.autootpAppSettingStepDisable = false;
		                            $scope.autootpAppSettingNameDisable = false;
		                            $scope.autootpAppSettingDomainDisable = false;
		                            $scope.autootpAppSettingIpAddressDisable = false;
		                            $scope.autootpAppSettingProxyServerDomainDisable = false;
		                            $scope.autootpAppSettingEmailDisable = false;
		                            
								    $scope.autootpAppSettingSaveDisable = true;
								    $scope.autootpAppSettingDeleteDisable = true;
								
								    $scope.autootpDevcenterReloadDisable = true;
							    	$scope.autootpDevcenterRemailDisable = true;
							    	$scope.autootpDevcenterRemailSettingDisable = true;

								    $scope.autootpServerSettingSaveDisable = true;
								    $scope.autootpServerSettingClearDisable = true;
														
									/////////////////////////// kcAutootpDelete http success process
									Notifications.success("AutoOTP Data Delete completed.");
								} else {
										$scope.realm.attributes.autootpReturnServerProgress = "Setting email Resend Request error.[3]"; 
										Notifications.error("Setting email Resend Request error~!");
										return;
								}
								
	                        }, function(error) {
							    console.log(error);
							    $scope.realm.attributes.autootpReturnServerProgress = "Setting email Resend Request error.[4]"; 
								Notifications.error("Setting email Resend Request error~!");
							});



                        } else {
                            Notifications.error("Missing or invalid field(s). Please verify the fields in red.")
                            //ngValid.removeClass('error');
                            ngValid.parent().removeClass('has-error');

                            var ngInvalid = form.find('.ng-invalid');
                            //ngInvalid.addClass('error');
                            ngInvalid.parent().addClass('has-error');
                        }
                    }
                });
            })
        }
    }
});







module.directive('kcAutootpServerSave', function ($compile, $timeout, Notifications, $http) {
    var clickDelay = 500; // 500 ms

    return {
        restrict: 'A',
        link: function ($scope, elem, attr, ctrl) {
            elem.addClass("btn btn-primary");
            elem.attr("type","submit");

            var disabled = false;
            elem.on('click', function(evt) {
                if ($scope.hasOwnProperty("changed") && !$scope.changed) return;

                // KEYCLOAK-4121: Prevent double form submission
                if (disabled) {
                    evt.preventDefault();
                    evt.stopImmediatePropagation();
                    return;
                } else {
                    disabled = true;
                    $timeout(function () { disabled = false; }, clickDelay, false);
                }

                $scope.$apply(function() {
                    var form = elem.closest('form');
                    if (form && form.attr('name')) {
                        var ngValid = form.find('.ng-valid');
                        if ($scope[form.attr('name')].$valid) {
//						if($scope.realm.attributes.autootpServerSettingAppServerKey.length > 0) { 
                            //ngValid.removeClass('error');
                            ngValid.parent().removeClass('has-error');
                            $scope['autootpsave']();
                            $scope.autootpServerSettingSaveDisable = true;
							$scope.autootpServerSettingClearDisable = false;
									
                        } else {
                            Notifications.error("Missing or invalid field(s). Please verify the fields in red.")
                            //ngValid.removeClass('error');
                            ngValid.parent().removeClass('has-error');

                            var ngInvalid = form.find('.ng-invalid');
                            //ngInvalid.addClass('error');
                            ngInvalid.parent().addClass('has-error');
                        }
                    }
                });
            })
        }
    }
});


module.directive('kcAutootpServerClear', function ($compile, $timeout, Notifications, $http) {
    var clickDelay = 500; // 500 ms

    return {
        restrict: 'A',
        link: function ($scope, elem, attr, ctrl) {
            elem.addClass("btn btn-primary");
            elem.attr("type","submit");

            var disabled = false;
            elem.on('click', function(evt) {
                if ($scope.hasOwnProperty("changed") && !$scope.changed) return;

                // KEYCLOAK-4121: Prevent double form submission
                if (disabled) {
                    evt.preventDefault();
                    evt.stopImmediatePropagation();
                    return;
                } else {
                    disabled = true;
                    $timeout(function () { disabled = false; }, clickDelay, false);
                }

                $scope.$apply(function() {
                    var form = elem.closest('form');
                    if (form && form.attr('name')) {
                        var ngValid = form.find('.ng-valid');
                        if ($scope[form.attr('name')].$valid) {
                            //ngValid.removeClass('error');
                            ngValid.parent().removeClass('has-error');
                            
                            $scope.realm.attributes.autootpServerSettingAppServerKey = "";
                            $scope.realm.attributes.autootpServerSettingAuthServerDomain = "";
                            

                        } else {
                            Notifications.error("Missing or invalid field(s). Please verify the fields in red.")
                            //ngValid.removeClass('error');
                            ngValid.parent().removeClass('has-error');

                            var ngInvalid = form.find('.ng-invalid');
                            //ngInvalid.addClass('error');
                            ngInvalid.parent().addClass('has-error');
                        }
                    }
                });
            })
        }
    }
});


