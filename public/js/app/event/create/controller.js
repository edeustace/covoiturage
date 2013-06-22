'use strict';




/* Controllers */

function EventCreationCtrl($scope, $http, $location) {
	$scope.evenement = {};
	var scope = $scope;
	$scope.items = [
	                  { id: "CAR", name: 'en voiture' },
	                  { id: "AUTOSTOP", name: 'à pied' },
	                  { id: "DONT_KNOW_YET", name: 'Je ne sais pas encore' }
                    ];
	$http.get('/rest/users/current').success(function(user) {
		if(user){
			user.lastLogin = null;
			scope.event = {creator:user, creatorRef:user.id};
			scope.event.creator.locomotion = "CAR";
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

