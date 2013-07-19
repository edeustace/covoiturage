'use strict';




/* Controllers */

function EventCreationCtrl($scope, $http, $location) {
	$scope.event = {};
	$scope.minDate = new Date();
	$scope.items = [
	                  { id: "CAR", name: 'en voiture' },
	                  { id: "AUTOSTOP", name: 'à pied' },
	                  { id: "DONT_KNOW_YET", name: 'Je ne sais pas encore' }
                    ];
	$scope.addContact = function(){	
		if(!$scope.event){
			$scope.event.contacts = new Array();
		}
		if($scope.contact.indexOf(";")>0){
			var reg=new RegExp("[ ,;]+", "g");
			var emails = $scope.contact.split(reg);
			for ( var int = 0; int < emails.length; int++) {
				var email = emails[int];
				if(validerEmail(email)){
					$scope.event.contacts.push(email);	
				}
			}
		}else{
			if(validerEmail($scope.contact)){
				$scope.event.contacts.push($scope.addedContact);	
			}
		}
		$scope.contact = null;
	};
	function validerEmail(mailtest){
		var reg = new RegExp('^[a-z0-9]+([_|\.|-]{1}[a-z0-9]+)*@[a-z0-9]+([_|\.|-]{1}[a-z0-9]+)*[\.]{1}[a-z]{2,6}$', 'i');
		if(reg.test(mailtest)){
			return(true);
		}else{
			return(false);
		}
	}
	
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
		}
	}).error(function(error){
		alert('error : '+error);
	});
	$scope.valider = function(){
		var theEvent = $scope.event;
		$http.post('/rest/events', theEvent).success(function(data){
			alert('done '+data.name);
			window.location = /evenement/+data.id;
		}).error(function(data){
		    var msg = '';
            if(data.errors){
                var someErrors = data.errors;
                for(var anError in someErrors){
                    msg += anError + ' : '+someErrors[anError]+'<br/>';
                }
                alert(msg);
            }else{
                alert('error '+data);
            }
		});
	}
}
// EventCtrl.$inject = ['$scope','register'];

