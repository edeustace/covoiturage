'use strict';




/* Controllers */

function EventCreationCtrl($scope, $http, $location) {
	$scope.evenement = {};
	var scope = $scope;
	$http.get('/rest/users/current').success(function(user) {
		if(user){
			user.lastLogin = null;
			scope.event = {creator:user, creatorRef:user.id};
			scope.creator = user;
		}
	}).error(function(error){
		alert('error : '+error);
	});
	$scope.valider = function(){
		//$scope.evenement.subscriber = [];
		//$scope.creator.creator = true;
		//$scope.evenement.subscriber.push($scope.creator);
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

