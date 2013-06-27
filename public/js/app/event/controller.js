'use strict';




/* Controllers */

function EventCtrl($scope, $http, marker, $location) {
	var _scope = $scope;
	var _marker = marker; 
	var _eventLinks;
    $scope.addPassenger = function(car, passenger){
    	var link = getCarLink(car);
    	$http.post(link, {passenger:passenger.userRef}).success(function(subscriber){
			var subscribersLink = _eventLinks["subscribers"];
			$http.get(subscribersLink).success(function (subscribers){
				if(subscribers){
					initSubscribers($scope, subscribers, marker);
					$scope.event.subscribers = subscribers;
				}
			}).error(function(error){
				alert(error);
			});
		}).error(function(error){
			alert("Error "+error);
		});
	}
    $scope.removePassenger = function(car, passenger){
    	var link = getCarLink(car);
    	$http.delete(link+'/'+passenger.userRef).success(function(subscriber){
			var subscribersLink = _eventLinks["subscribers"];
			$http.get(subscribersLink).success(function (subscribers){
				if(subscribers){
					initSubscribers($scope, subscribers, marker);
					$scope.event.subscribers = subscribers;
				}
			}).error(function(error){
				alert(error);
			});
		}).error(function(error){
			alert("Error "+error);
		});
	}
    
    marker.initMaps($scope);
    var id = extractFromUrl($location.absUrl());
    $http.get('/rest/users/current').success(function(user) {
		if(user){
			
			//TODO si le user courant ne fait pas parti des subscriber il faut lui proposer de s'ajouter 
			
			$scope.user = user;
			$http.get('/rest/events/'+id).success(function(event) {
			    if(event){
			    	$scope.event = event;
			    	marker.reinitMarker();
			    	_eventLinks = getEventLinks(event);
				   	if(event){
				        marker.recordEvent(event);
				   	
					   	if(event.subscribers){
					   		initSubscribers($scope, event.subscribers, marker, function(subscriber){
					   			marker.recordSubscriber(subscriber);
					   			var idUser = $scope.user.id;
								if(idUser == subscriber.userRef){
									if(subscriber.locomotion=="CAR"){
										$scope.filterUsers = "AUTOSTOP";	
									}else if(subscriber.locomotion=="AUTOSTOP"){
										$scope.filterUsers = "CAR";
									}
								}
					   		});
				       	}
				   	}
					var markers = $scope.markers;
					$scope.$watch('filterUsers', function(newValue, oldValue) {
							if(newValue){ 
								for(var i=0;i<$scope.markers.length;i++){
									var marker = markers[i];
									if(!(marker.type === newValue) && (marker.type != "EVENT")){
										marker.visible = false; 
									}else{
										marker.visible = true; 
									}
								}		
							}else{
								for(var i=0;i<$scope.markers.length;i++){
									var marker = markers[i];
									marker.visible = true; 
								}		
							}	
					});
			    }
		   });
		}
	}).error(function(error){
		alert('error : '+error);
	});
    function extractFromUrl(url){
        var index = url.lastIndexOf("/")
        return url.substring(index+1, url.length);
    }
    function initSubscribers($scope, subscribers, marker, callback){
		var length = subscribers.length;
		var idUser = $scope.user.id;
		$scope.refSubscribers = {};
	   	for(var i=0; i<length; i++){
			var subscriber = subscribers[i];
			$scope.refSubscribers[subscriber.userRef] = subscriber; 
			if(subscriber.locomotion && subscriber.locomotion==="CAR"){
				subscriber.picto = marker.pictoAuto;
			}else if(subscriber.locomotion && subscriber.locomotion==="AUTOSTOP"){
				subscriber.picto = marker.pictoStop;			
			}else{
			}
			if(idUser == subscriber.userRef){
				$scope.currentSubscriber = subscriber;
				subscriber.current = true;
			}else{
				if(subscriber.car && subscriber.car.passengers && inArray(idUser, subscriber.car.passengers)){
					subscriber.currentCar = true;
				}else{
					subscriber.currentCar = false;
				}
				subscriber.current = false;
			}

			if(callback){
				callback(subscriber);
			}
   	 	}
	   	if($scope.currentSubscriber.locomotion == 'CAR'){
	   		for(var i=0; i<length; i++){
	   			var subscriber = subscribers[i];
	   			if(subscriber.carRef == $scope.currentSubscriber.userRef){
	   				subscriber.inMyCar = true;		
	   			}else{
	   				subscriber.inMyCar = false;
	   			}
	   		}
	   	}
	   		
	   	$scope.subscribers = subscribers;
    }
    function inArray(value, array){
    	if(array && value){
    		for ( var int = 0; int < array.length; int++) {
				var arrayValue = array[int];
				if(arrayValue == value){
					return true;
				}
			}
    	}else{
    		return false;
    	}
    }
    function getPassengersLink(subscriber){
		if(subscriber.links){
			for ( var int = 0; int < subscriber.links.length; int++) {
				var link = subscriber.links[int];
				if(link && link.rel == "passengers"){
					return link.href;
				}
			}
		}
		return null;
    }
    function getEventLinks(event){
    	var theLinks = {};
    	if(event.links){
    		for ( var i = 0; i < event.links.length; i++) {
				var link = event.links[i];
				theLinks[link.rel] = link.href;
			}
    	}
    	return theLinks;
    }
    function getCarLink(subscriber){
		if(subscriber.car && subscriber.car.links){
			for ( var int = 0; int < subscriber.car.links.length; int++) {
				var link = subscriber.car.links[int];
				if(link && link.rel == "self"){
					return link.href;
				}
			}
		}
		return null;
    }
}
//EventCtrl.$inject = ['$scope','register'];

