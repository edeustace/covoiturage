'use strict';




/* Controllers */

function EventCreationCtrl($scope, $http) {
	$scope.evenement = {}
	$scope.creator = {}
	$scope.valider = function(){
		//$scope.evenement.subscriber = [];
		//$scope.creator.creator = true;
		//$scope.evenement.subscriber.push($scope.creator);
		var theEvent = $scope.event;
		$http.post('/rest/events', theEvent).success(function(data){
			alert('done '+data.name);
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

