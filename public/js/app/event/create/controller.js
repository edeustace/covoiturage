'use strict';




/* Controllers */

function EventCreationCtrl($scope, $http, $location, mailUtils, errorService) {
	$scope.event = {};
	$scope.alerts = new Array();
	$scope.minDate = new Date();
	$scope.items = [
	                  { id: "CAR", name: 'en voiture' },
	                  { id: "AUTOSTOP", name: 'à pied' },
	                  { id: "DONT_KNOW_YET", name: 'Je ne sais pas encore' }
                    ];
	$scope.addContact = function(){
	    if(!$scope.event.contacts){
            $scope.event.contacts = new Array();
	    }

		$scope.event.contacts = mailUtils.pushMails($scope.contact, $scope.event.contacts);
		$scope.contact = null;
	};

	$scope.remove = function(index){
		$scope.event.contacts.splice(index, 1);
	}
	
	$http.get('/rest/users/current').success(function(user) {
		if(user){
			user.lastLogin = null;
			//$scope.event = {creator:user, creatorRef:user.id};
			$scope.event = {creatorRef:user.id};
			//$scope.event.creator.locomotion = "CAR";
			$scope.event.fromDate = $scope.minDate;
			$scope.event.startTime = "";
		}
	}).error(function(error){
		alert('error : '+error);
	});
	$scope.valider = function(){
		var theEvent = $scope.event;
		$http.post('/rest/events', theEvent).success(function(data){
			window.location = /evenement/+data.id;
		}).error(function(data){
		    $scope.alerts = new Array();
		    errorService.formatErrors(data, function(msg){
               $scope.alerts.push({type:"error", msg:msg});
           });
		});
	};

	$scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };
}
// EventCtrl.$inject = ['$scope','register'];

