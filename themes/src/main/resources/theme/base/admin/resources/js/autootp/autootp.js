
module.controller('AutoOTPCtrl', function($scope, $http)
{
	if($scope.realm.attributes.autootpAppSettingStep.length > 0) { 
		$scope.autootpAppSettingStepDisable = true;
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

	if($scope.realm.attributes.autootpReturnDomainValidationToken.length > 0) { 
    	$scope.autootpDevcenterReloadDisable = false;
	} else { 
		$scope.autootpDevcenterReloadDisable = true;
	}  

    $scope.autootpServerSettingSaveDisable = true;
    $scope.autootpServerSettingClearDisable = false;
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
                            
                            $scope['autootpsave']();
                            
                            $scope.autootpAppSettingStepDisable = true;
                            $scope.autootpAppSettingNameDisable = true;
                            $scope.autootpAppSettingDomainDisable = true;
                            $scope.autootpAppSettingIpAddressDisable = true;
                            $scope.autootpAppSettingProxyServerDomainDisable = true;
                            $scope.autootpAppSettingEmailDisable = true;

                            $scope.autootpAppSettingSaveDisable = true;
                            $scope.autootpAppSettingDeleteDisable = false;
                            
                            $scope.autootpDevcenterReloadDisable = false;

							$http.get('http://localhost:8081/auth/')
					                        .then(function (response) {
												console.log(response);
					                            $scope.realm.attributes.autootpReturnDomainValidationToken = "AWEROLIUAHWLELDBVLUZWHERFLSNFLAWUELASHFLAKSDJHF";
					                            $scope.realm.attributes.autootpReturnServerProgress = "등록정보 심사중...";
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
							
							    $scope.autootpServerSettingSaveDisable = true;
							    $scope.autootpServerSettingClearDisable = false;
	
								$http.get('http://localhost:8081/auth/')
						                        .then(function (response) {
													console.log(response);
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
                            
                            $scope['autootpsave']();
                            
							$http.get('http://localhost:8081/auth/')
					                        .then(function (response) {
												console.log(response);
					                            $scope.autootpAppSettingStepDisable = true;
					                            $scope.autootpAppSettingSaveDisable = true;
					                            $scope.autootpAppSettingDeleteDisable = false;
					                            $scope.realm.attributes.autootpReturnDomainValidationToken = "AWEROLIUAHWLELDBVLUZWHERFLSNFLAWUELASHFLAKSDJHF";
					                            $scope.realm.attributes.autootpReturnServerProgress = "등록정보 심사완료~!";
					                            $scope.autootpServerSettingSaveDisable = false;
					                            Notifications.error("Server progress refresh completed.")
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


