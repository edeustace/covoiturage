'use strict';




/* Controllers */

function EventCtrl($scope, $http, marker, $location) {
	var _scope = $scope;
	var _marker = marker; 
	var _eventLinks;
	$scope.eventLinks = {};
	$scope.subscribersLinks = {};
	$scope.editMode = false;
	$scope.saveCurrentSubscriber = function(){
		$http.put($scope.subscribersLinks[$scope.currentSubscriber.userRef].self ,$scope.currentSubscriber).success(function(){
			$scope.setEditMode(false);
			marker.recordSubscriber($scope.currentSubscriber);
			reloadSubscribers();
		}).error(function(error){
			alert("Error "+error);
		});
	}
    $scope.addPassenger = function(car, passenger){
    	var link = getCarLink(car);
    	$http.post(link, {passenger:passenger.userRef}).success(function(subscriber){
    		reloadSubscribers();
		}).error(function(error){
			alert("Error "+error);
		});
	}
    $scope.setEditMode = function(value){
    	$scope.editMode = value;	
    }
    $scope.removePassenger = function(car, passenger){
    	var link = getCarLink(car);
    	$http.delete(link+'/'+passenger.userRef).success(function(subscriber){
			reloadSubscribers();
		}).error(function(error){
			alert("Error "+error);
		});
	}
    
    function reloadSubscribers(){
    	var subscribersLink = $scope.eventLinks.subscribers;
		$http.get(subscribersLink).success(function (subscribers){
			if(subscribers){
				initSubscribers($scope, subscribers, marker);
				$scope.event.subscribers = subscribers;
			}
		}).error(function(error){
			alert(error);
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
			    	$scope.eventLinks = buildLinks(event.links);
			    	event.picto = $scope.eventLinks.picto;
				   	if(event){
				        marker.recordEvent(event);
				   	
					   	if(event.subscribers){
					   		initSubscribers($scope, event.subscribers, function(subscriber){
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
				   	var wsUrl = jsRoutes.controllers.SubscriberCtrl.subscribersUpdates(event.id, $scope.currentSubscriber.userRef);
				   	var ws = new WebSocket(wsUrl.webSocketURL());
				    
				    ws.onopen = function(){  
				        console.log("Socket has been opened!");  
				    };
				    
				    ws.onmessage = function(message) {
				    	var newSubscriber = JSON.parse(message.data);
				    	updateSubscriber($scope, newSubscriber, function(subscriber){
				    		_marker.recordSubscriber(subscriber);
				   		});
				    	var newSubscribers = new Array();
				    	var find = false;
				    	for ( var indice in $scope.subscribers) {
							if($scope.subscribers[indice].userRef == newSubscriber.userRef){
								newSubscribers.push(newSubscriber);
								find = true;
							}else{
								newSubscribers.push($scope.subscribers[indice]);
							}
						}
				    	if(!find){
				    		newSubscribers.push(newSubscriber);
				    	}
				    	$scope.subscribers = newSubscribers;
				    	$scope.$apply();
				    };
				   	
					var markers = $scope.markers;
					$scope.$watch('filterUsers', function(newValue, oldValue) {
							if(newValue){ 
								for(var i=0;i<$scope.markers.length;i++){
									var marker = markers[i];
									if(!(marker.type === newValue) && (marker.type != "EVENT") && !marker.subscriber.current){
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
    function updateSubscriber($scope, subscriber, callback){
    	var idUser = $scope.user.id;
    	$scope.subscribersLinks[subscriber.userRef] = buildLinks(subscriber.links);
		$scope.refSubscribers[subscriber.userRef] = subscriber; 
		subscriber.picto = $scope.subscribersLinks[subscriber.userRef].picto;
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
    function initSubscribers($scope, subscribers, callback){
		var length = subscribers.length;
		var idUser = $scope.user.id;
		$scope.refSubscribers = {};
	   	for(var i=0; i<length; i++){
			var subscriber = subscribers[i];
			updateSubscriber($scope, subscriber, callback)
			
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

