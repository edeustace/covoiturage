'use strict';




/* Controllers */

function UserCtrl($scope, $http) {
	$scope.alerts = [];
	$scope.saveUser = function(){
	  	$http.put('/rest/users/'+$scope.user.id, $scope.user).success(function(events) {
            $scope.alerts.push({ type: 'success', msg: 'votre compte a été modifié'});
	  	}).error(function(data){
	  	    if(data.errors){
                var obj = JSON.parse(JSON.stringify(data.errors));
                var someErrors = data.errors;
                for(var anError in someErrors){
                    var msgs = someErrors[anError];
                    for(var i in msgs){
                        $scope.alerts.push({ type: 'error', msg: msgs[i]});
                    }
                }
            }else{
                $scope.alerts.push({ type: 'error', msg: data});
            }
	  	});
	};
	$scope.closeAlert = function(index) {
		$scope.alerts.splice(index, 1);
	};


	$http.get('/rest/users/current').success(function(user) {
		if(user){
			$scope.user = user;
			$http.get('/rest/users/'+user.id+'/events').success(function(events) {
				$scope.events = events;
				convertEvents($scope.events);
			}).error(function(error){
				alert('error : '+error);
			});

			$http.get('/rest/users/'+user.id+'/invitations').success(function(events) {
                $scope.invitations = events;
                convertEvents($scope.invitations);
            }).error(function(error){
                alert('error : '+error);
            });
		}
	}).error(function(error){
		alert('error : '+error);
	});

	function convertEvents(events){
	    for ( var i = 0; i < events.length; i++) {
            convertEvent(events[i]);
        }
	}

	function convertEvent(event){
	    event.links = buildLinks(event.links);
        if(user.id == event.creatorRef){
            event.isCreator = true;
        }
	}

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