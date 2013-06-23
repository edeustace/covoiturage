'use strict';




/* Controllers */

function EventCreationCtrl($scope, $http, $location) {
	
	$scope.mode = "CREATE";
	var scope = $scope;
	$scope.items = [
	                  { id: "CAR", name: 'en voiture' },
	                  { id: "AUTOSTOP", name: 'à pied' },
	                  { id: "DONT_KNOW_YET", name: 'Je ne sais pas encore' }
                    ];
	$http.get('/rest/users/current').success(function(user) {
		if(user){
			user.lastLogin = null;
			scope.subscriber = user;
			scope.subscriber.userRef = user.id;
			scope.subscriber.locomotion = "CAR";
		}
	}).error(function(error){
		alert('error : '+error);
	});
	
	var idEvent = getIdEvent($location.absUrl());
	$http.get('/rest/events/'+idEvent).success(function(event) {
		if(event){
			scope.event = event;
		}
	}).error(function(error){
		alert('error : '+error);
	});
	
	$scope.update = function(){
		if($scope.subscriber.id){
			$http.put('/rest/events/'+idEvent+'/subscribers/'+$scope.subscriber.id, $scope.subscriber).success(function(data){
				alert('Ok !');
				window.location = /evenement/+idEvent;
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
		}else{
			alert("une erreur s'est produite");
		}
	}
	
	$scope.cancel = function(){
		$scope.message=null;
	}
	
	$scope.create = function(){
		$http.post('/rest/events/'+idEvent+'/subscribers/', $scope.subscriber).success(function(data){
			alert('Ok !');
			window.location = /evenement/+idEvent;
		}).error(function(data){
			if(data.errors){
                var someErrors = data.errors;
                for(var anError in someErrors){
                    msg += anError + ' : '+someErrors[anError]+'<br/>';
                }
                $scope.message = msg;
            }
			if(data.id){
				$scope.subscriber.id = data.id
				$scope.message = data.message;
			}
		});
	}
	
	function getIdEvent(currentUrl){
		var debut = currentUrl.indexOf("/evenement/")+"/evenement/".length;
		var fin = currentUrl.indexOf("/participer");
		return currentUrl.substring(debut, fin);
	}
}
// EventCtrl.$inject = ['$scope','register'];

