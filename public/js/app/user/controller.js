'use strict';




/* Controllers */

function UserCtrl($scope, $http) {
	$http.get('/rest/users/current').success(function(user) {
		if(user){
			$http.get('/rest/users/'+user.id+'/events').success(function(events) {
				$scope.events = events;
				for ( var int = 0; int < $scope.events.length; int++) {
					var event = $scope.events[int];
					event.links = buildLinks(event.links);
				}
			}).error(function(error){
				alert('error : '+error);
			});
		}
	}).error(function(error){
		alert('error : '+error);
	});
	function buildLinks(links){
    	var theLinks = {};
    	if(links){
    		for ( var i = 0; i < links.length; i++) {
				var link = links[i];
				theLinks[link.rel] = link.href;
			}
    	}
    	return theLinks;
    }
}