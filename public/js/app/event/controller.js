'use strict';




/* Controllers */

function EventCtrl($scope, $http, $location, $compile) {
	var _scope = $scope;
	var _eventLinks;
	$scope.myMarkers = [];
	$scope.mapOptions = {
	          center: new google.maps.LatLng(46.65278, -1.424961),
	          zoom: 9,
	          mapTypeId: google.maps.MapTypeId.ROADMAP
	        };
	$scope.bounds = new google.maps.LatLngBounds();
	$scope.eventLinks = {};
	$scope.subscribersLinks = {};
	$scope.editMode = false;
	$scope.alerts = [];
	$scope.directionsDisplay = new google.maps.DirectionsRenderer();
	$scope.directionsService = new google.maps.DirectionsService();
	$scope.closeAlert = function(index) {
		$scope.refSubscribers[$scope.alerts[index].userRef].class = null;
	    $scope.alerts.splice(index, 1);
	  };
	$scope.saveCurrentSubscriber = function(){
		$http.put($scope.subscribersLinks[$scope.currentSubscriber.userRef].self ,$scope.currentSubscriber).success(function(){
			$scope.setEditMode(false);
			addMarkerSubscriber($scope.currentSubscriber);
			reloadSubscribers();
			
		}).error(function(error){
			alert("Error "+error);
		});
	};
    $scope.addPassenger = function(car, passenger){
    	var link = getCarLink(car);
    	$http.post(link, {passenger:passenger.userRef}).success(function(subscriber){
    		reloadSubscribers();
		}).error(function(error){
			alert("Error "+error);
		});
	};
    $scope.setEditMode = function(value){
    	$scope.editMode = value;	
    };
    $scope.removePassenger = function(car, passenger){
    	var link = getCarLink(car);
    	$http.delete(link+'/'+passenger.userRef).success(function(subscriber){
			reloadSubscribers();
		}).error(function(error){
			alert("Error "+error);
		});
	};
	$scope.openMarkerInfo = function(marker) {
		$scope.currentMarker = marker;
		$scope.currentInfoWindowsSubscriber = marker.subscriber;
        $scope.myInfoWindow.open($scope.myMap, marker);
        $scope.$apply();
    };
    function traceDirections(){
    	if($scope.currentSubscriber.locomotion == "CAR"){
    		$scope.directionsDisplay.setMap($scope.myMap);
    		var start = new google.maps.LatLng($scope.currentSubscriber.address.location.lat, $scope.currentSubscriber.address.location.lng);
    		var end = new google.maps.LatLng($scope.event.address.location.lat, $scope.event.address.location.lng);
    		var waypts = [];
    		  if($scope.currentSubscriber.car.passengers){
    		  for (var i = 0; i < $scope.currentSubscriber.car.passengers.length; i++) {
    			  var passenger = $scope.refSubscribers[$scope.currentSubscriber.car.passengers[i]];
    			  var loc = new google.maps.LatLng(passenger.address.location.lat, passenger.address.location.lng)
    		      waypts.push({
    		    	  location:loc, 
    		          stopover:false
    		      });
    		    }
    		  }
    		var request = {
    		      origin:start,
    		      destination:end,
    		      waypoints: waypts,
    		      optimizeWaypoints: true,
    		      travelMode: google.maps.DirectionsTravelMode.DRIVING
    		  };
    		  $scope.directionsService.route(request, function(response, status) {
    		    if (status == google.maps.DirectionsStatus.OK) {
    		    	$scope.directionsDisplay.setDirections(response);
    		    }
    		  });
    	}
    }
    function setCurrentWidowsSubscriber(){
    	if($scope.currentInfoWindowsSubscriber){
    		var marker = findMarkerByUserRef($scope.currentInfoWindowsSubscriber.userRef);
    		var subscriber = $scope.refSubscribers[$scope.currentInfoWindowsSubscriber.userRef];
    		if(marker && subscriber){
    			$scope.currentMarker = marker;
                $scope.currentInfoWindowsSubscriber = subscriber;
    		}
    	}
    } 
    function reloadSubscribers(){
    	var subscribersLink = $scope.eventLinks.subscribers;
		$http.get(subscribersLink).success(function (subscribers){
			if(subscribers){
				initSubscribers($scope, subscribers, function(subscriber){
		   		});
				$scope.event.subscribers = subscribers;
				setCurrentWidowsSubscriber();
			}
		}).error(function(error){
			alert(error);
		});
    }
    
    var id = extractFromUrl($location.absUrl());
    $http.get('/rest/users/current').success(function(user) {
		if(user){
			
			//TODO si le user courant ne fait pas parti des subscriber il faut lui proposer de s'ajouter 
			
			$scope.user = user;
			$http.get('/rest/events/'+id).success(function(event) {
			    if(event){
			    	$scope.event = event;
			    	$scope.eventLinks = buildLinks(event.links);
			    	event.picto = $scope.eventLinks.pictoFinish;
				   	if(event){
				        addMarkerEvent(event);
					   	if(event.subscribers){
					   		initSubscribers($scope, event.subscribers, function(subscriber){
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
				   	$scope.myMap.fitBounds($scope.bounds);
				   	traceDirections();
				   	var wsUrl = jsRoutes.controllers.SubscriberCtrl.subscribersUpdates(event.id, $scope.currentSubscriber.userRef);
				   	var ws = new WebSocket(wsUrl.webSocketURL());
				    
				    ws.onopen = function(){  
				        console.log("Socket has been opened!");  
				    };
				    
				    ws.onmessage = function(message) {
				    	var newSubscriber = JSON.parse(message.data);
				    	updateSubscriber($scope, newSubscriber, function(subscriber){
				    		addMarkerSubscriber(newSubscriber);
				   		});
				    	$scope.alerts.push({
			    			msg : newSubscriber.surname + " " +newSubscriber.name + " a modifié ses caracteristiques", 
			    			type : "success",
			    			userRef : newSubscriber.userRef
				    	});
				    	newSubscriber.class = "success";
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
				   	
					$scope.$watch('filterUsers', function(newValue, oldValue) {
						for(var i=0;i<$scope.myMarkers.length;i++){
							var marker = $scope.myMarkers[i];
							if(newValue){
								if(!(marker.type === newValue) && (marker.type != "EVENT") && !marker.subscriber.current){
									marker.setVisible(false); 
								}else{
									marker.setVisible(true); 
								}
							}else{
								marker.setVisible(true);
							}
						}
							
					});
			    }
		   });
		}
	}).error(function(error){
		alert('error : '+error);
	});
    
    function addMarkerEvent(event){
    	$scope.mapOptions.center = new google.maps.LatLng(event.address.location.lat, event.address.location.lng);
    	
    	var marker = new google.maps.Marker({
            map: $scope.myMap,
            position: new google.maps.LatLng(event.address.location.lat, event.address.location.lng),
            icon:event.picto, 
            visible : true
          });
    	marker.type = "EVENT";
    	$scope.myMarkers.push(marker);
    	$scope.bounds.extend(marker.getPosition());
    }
    function addMarkerSubscriber(subscriber){
    	var marker = findMarkerByLatLng(subscriber.address.location.lat, subscriber.address.location.lng);
    	if(marker){
    		marker.setVisible(subscriber.visible);
    		marker.setIcon(subscriber.picto);
    		marker.setVisible(subscriber.visible);
    	}else{
    		marker = findMarkerByUserRef(subscriber.address.location.lat, subscriber.address.location.lng);
    		if(marker){
    			marker.setPosition(new google.maps.LatLng(subscriber.address.location.lat, subscriber.address.location.lng));
    			marker.setVisible(subscriber.visible);
    			marker.setIcon(subscriber.picto);
    		}else{
	        	marker = new google.maps.Marker({
	                map: $scope.myMap,
	                position: new google.maps.LatLng(subscriber.address.location.lat, subscriber.address.location.lng),
	                icon:subscriber.picto, 
	                visible : subscriber.visible
	              });
	        	marker.type = subscriber.locomotion;
	        	$scope.myMarkers.push(marker);
	        	$scope.bounds.extend(marker.getPosition());
    		}
    	}
    	marker.type = subscriber.locomotion;
    	marker.subscriber = subscriber;
    }
    function findMarkerByLatLng(lat, lng) {
		for ( var i = 0; i < $scope.myMarkers.length; i++) {
			var pos = $scope.myMarkers[i].getPosition();
			if (floatEqual(pos.lat(), lat)
					&& floatEqual(pos.lng(), lng)) {
				return $scope.myMarkers[i];
			}
		}

		return null;
	};
	function findMarkerByUserRef(ref) {
		for ( var i = 0; i < $scope.myMarkers.length; i++) {
			var subscriber = $scope.myMarkers[i].subscriber;
			if(subscriber && subscriber.userRef == ref){
				return $scope.myMarkers[i];
			}
		}
		return null;
	}
	
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
			$scope.currentSubscriber.visible=true;
			subscriber.current = true;
		}else{
			subscriber.current = false;
			if($scope.filterUsers == "AUTOSTOP"){
				if(subscriber.locomotion!="AUTOSTOP"){
					subscriber.visible = false;	
				}else{
					subscriber.visible = true;
				}
			}else if($scope.filterUsers == "CAR"){
				if(subscriber.locomotion!="CAR"){
					subscriber.visible = false;	
				}else{
					subscriber.visible = true;
				}
			}
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
   		for(var i=0; i<length; i++){
   			var subscriber = subscribers[i];
   			if(subscriber.locomotion=='AUTOSTOP'){
   				if(subscriber.carRef == $scope.currentSubscriber.userRef){
	   				subscriber.inMyCar = true;		
	   				subscriber.picto = $scope.eventLinks.pictoStopLight;
	   			}else{
	   				subscriber.inMyCar = false;
	   				subscriber.picto = $scope.eventLinks.pictoStop;
	   			}	
   			}else if(subscriber.locomotion=='CAR'){
   				if($scope.currentSubscriber.locomotion=='AUTOSTOP' && $scope.currentSubscriber.carRef == subscriber.userRef){
   					subscriber.picto = $scope.eventLinks.pictoCarLight;
   					subscriber.currentCar = true;
   				}else{
   					subscriber.picto = $scope.eventLinks.pictoCar;
   					subscriber.currentCar = false;
   				}
   			}else{
   				subscriber.picto = $scope.eventLinks.pictoDontKnow;
   			}
   			addMarkerSubscriber(subscriber);
   		}
   		
   		if($scope.currentSubscriber.locomotion=='CAR'){
   			$scope.currentSubscriber.picto = $scope.eventLinks.pictoCarLight;	
   		}else if($scope.currentSubscriber.locomotion=='AUTOSTOP'){
   			$scope.currentSubscriber.picto = $scope.eventLinks.pictoStopLight;
   		}
   		
   		if(callback){
		   		for(var i=0; i<length; i++){
		   			var subscriber = subscribers[i];
		   			callback(subscriber);
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
    function floatEqual(f1, f2) {
		return (Math.abs(f1 - f2) < 0.000001);
	}
}
//EventCtrl.$inject = ['$scope','register'];

