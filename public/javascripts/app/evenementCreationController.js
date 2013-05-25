'use strict';




/* Controllers */

function EventCreationCtrl($scope, $http) {
	$scope.evenement = {}
	$scope.creator = {}
	$scope.valider = function(){
		//$scope.evenement.subscriber = [];
		//$scope.creator.creator = true;
		//$scope.evenement.subscriber.push($scope.creator);
		var data = $scope.evenement;
		$http.post('/rest/evenement', data).success(function(data){
			alert('done '+data.name);
		}).error(function(data){
			alert('error '+data);
		});
	}
}
// EventCtrl.$inject = ['$scope','register'];

