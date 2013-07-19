'use strict';




/* Controllers */

function EventCtrl($scope, $http, $location, $compile) {
	//////////////  ATTRIBUTS  ///////////////////
	$scope.items = [
	                  { id: "CAR", name: 'en voiture' },
	                  { id: "AUTOSTOP", name: 'à pied' },
	                  { id: "DONT_KNOW_YET", name: 'Je ne sais pas encore' }
                  ];
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
	
	//////////////  SCOPE METHODS  ///////////////////	
	$scope.subscribe = function(){
		if($scope.newSubscriber && $scope.newSubscriber.userRef){
			$http.post($scope.eventLinks.subscribers, $scope.newSubscriber).success(function(data){
				$scope.alerts.push({type:"success", msg:"Vous participez à l'événement !"});			
				reloadSubscribers($scope);
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
	};
	$scope.addContact = function(){	
		if(!$scope.newContacts){
			$scope.newContacts = new Array();
		}
		if($scope.addedContact.indexOf(";")>0){
			var reg=new RegExp("[ ,;]+", "g");
			var emails = $scope.addedContact.split(reg);
			for ( var int = 0; int < emails.length; int++) {
				var email = emails[int];
				if(validerEmail(email)){
					$scope.newContacts.push(email);	
				}
			}
		}else{
			if(validerEmail($scope.addedContact)){
				$scope.newContacts.push($scope.addedContact);	
			}
		}
		$scope.addedContact = null;
	};
	function validerEmail(mailtest){
		var reg = new RegExp('^[a-z0-9]+([_|\.|-]{1}[a-z0-9]+)*@[a-z0-9]+([_|\.|-]{1}[a-z0-9]+)*[\.]{1}[a-z]{2,6}$', 'i');
		if(reg.test(mailtest)){
			return(true);
		}else{
			return(false);
		}
	}
	$scope.send = function(){
		$http.post($scope.eventLinks.contacts, {contacts:$scope.newContacts}).success(function(){
			for ( var int = 0; int < $scope.newContacts.length; int++) {
				var contact = $scope.newContacts[int];
				$scope.event.contacts.push(contact);
			}
			$scope.newContacts = new Array();
		}).error(function(error){
			alert("Error "+error);
		});
	};
	$scope.remove = function(index){
		$scope.newContacts.splice(index, 1);
	};
	$scope.closeAlert = function(index) {
		if($scope.alerts[index].notification && $scope.alerts[index].notification.id){
			if($scope.currentSubscriber){
				var link = $scope.subscribersLinks[$scope.currentSubscriber.userRef].notifications;
				$http.delete(link+'/'+$scope.alerts[index].notification.id);
			}
		}
	    $scope.alerts.splice(index, 1);
	  };
	
	$scope.saveCurrentSubscriber = function(){
		if($scope.currentSubscriber){
			$http.put($scope.subscribersLinks[$scope.currentSubscriber.userRef].self ,$scope.currentSubscriber).success(function(){
				$scope.setEditMode(false);
				addMarkerSubscriber($scope.currentSubscriber);
				reloadSubscribers($scope);
				
			}).error(function(error){
				alert("Error "+error);
			});
		}
	};
	//Interaction car / passenger
	$scope.getPropositions = function(){
		var result = new Array();
		for ( var indice in $scope.subscribers) {
			var subscriber = $scope.subscribers[indice];
			if(subscriber.heAskMeToBeInHisCar){
				result.push(subscriber);
			}
		}
		return result;
	};
	//CAR OWNER 
    $scope.proposeSeat = function(passenger){
    	if($scope.currentSubscriber){
	    	var carRef = $scope.currentSubscriber.userRef;
	    	var link = $scope.subscribersLinks[passenger.userRef].addPossibleCar;
	    	$http.post(link, {car:carRef}).success(function(subscriber){
	    		reloadSubscribers($scope);
	    		$scope.myInfoWindow.close();
			}).error(function(error){
				alert("Error "+error);
			});
    	}
	};
    $scope.removeWaitingGuy = function(car, passenger){
    	var links = $scope.subscribersLinks[car.userRef];
    	if(links && links.carlinks && links.carlinks.waitings){
	    	var link = links.carlinks.waitings;
	    	$http.delete(link+'/'+passenger).success(function(subscriber){
				reloadSubscribers($scope);
				$scope.myInfoWindow.close();
			}).error(function(error){
				alert("Error "+error);
			});
    	}
	};
	$scope.validatePassenger = function(car, passenger){
    	var link = $scope.subscribersLinks[car.userRef].car;
    	$http.post(link, {passenger:passenger}).success(function(subscriber){
    		reloadSubscribers($scope);
    		$scope.myInfoWindow.close();
		}).error(function(error){
			alert("Error "+error);
		});
	};
	$scope.removePassenger = function(car, passenger){
    	var link = $scope.subscribersLinks[car.userRef].car;
    	$http.delete(link+'/'+passenger).success(function(subscriber){
			reloadSubscribers($scope);
			$scope.myInfoWindow.close();
		}).error(function(error){
			alert("Error "+error);
		});
	};
	
	//AUTOSTOPER 
	$scope.askForSeat = function(car){
		if($scope.currentSubscriber){
			var passenger = $scope.currentSubscriber.userRef;
	    	var links = $scope.subscribersLinks[car.userRef];
	    	if(links && links.carlinks && links.carlinks.waitings){
		    	var link = links.carlinks.waitings;
		    	$http.post(link, {passenger:passenger}).success(function(subscriber){
		    		reloadSubscribers($scope);
		    		$scope.myInfoWindow.close();
				}).error(function(error){
					alert("Error "+error);
				});
	    	}
		}
	};
	$scope.removePossibleCar = function(passenger, carRef){
		var link = $scope.subscribersLinks[passenger.userRef].addPossibleCar;
    	$http.delete(link+'/'+carRef).success(function(subscriber){
    		reloadSubscribers($scope);
    		$scope.myInfoWindow.close();
		}).error(function(error){
			alert("Error "+error);
		});
	};
    $scope.setEditMode = function(value){
    	$scope.editMode = value;	
    };
    
    $scope.openSubscriberInfo = function(subscriber) {
    	var marker = findMarkerByLatLng(subscriber.address.location.lat, subscriber.address.location.lng);
    	$scope.openMarkerInfo(marker);
    };
	$scope.openMarkerInfo = function(marker) {
		$scope.currentMarker = marker;
		$scope.currentInfoWindowsSubscriber = marker.subscriber;
        $scope.myInfoWindow.open($scope.myMap, marker);
        $scope.$apply();
    };
    
    
    
    ///////////////  INIT /////////////////////
    
    var id = extractFromUrl($location.absUrl());
    $http.get('/rest/users/current').success(function(user) {
		if(user){
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
				   	if(!$scope.currentSubscriber){
				   		$scope.newSubscriber = {
				   				userRef : $scope.user.id,  
				   				email : $scope.user.email,
				   				name : $scope.user.name,
				   				surname : $scope.user.surname,
				   				locomotion : 'CAR'
				   		};
				   	}
				   	
				   	$scope.myMap.fitBounds($scope.bounds);
				   	var wsUrl = jsRoutes.controllers.SubscriberCtrl.subscribersUpdates(event.id, $scope.user.id);
				   	var ws = new WebSocket(wsUrl.webSocketURL());
				    
				    ws.onopen = function(){  
				        console.log("Socket has been opened!");  
				    };
				    
				    ws.onmessage = function(wsMsg) {
				    	if(wsMsg && wsMsg.data){
				    		var notification = JSON.parse(wsMsg.data);
				    		if(notification.type && notification.message){
				    			$scope.alerts.push({type:notification.type, msg:notification.message, notification:notification});
				    		}
				    	}
				    	reloadSubscribers($scope);
				    };
				   	
				    if($scope.currentSubscriber && $scope.subscribersLinks[$scope.currentSubscriber.userRef] && $scope.subscribersLinks[$scope.currentSubscriber.userRef].notifications){
					    $http.get($scope.subscribersLinks[$scope.currentSubscriber.userRef].notifications).success(function(notifications) {
					    	for ( var int = 0; int < notifications.length; int++) {
								var notification = notifications[int];
								$scope.alerts.push({type:notification.type, msg:notification.message, notification:notification});
							}
					    });
				    }
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
    
    /////////   PRIVATE   ////////////////
    function traceDirections($scope){
    	var car = null;
    	if($scope.currentSubscriber && $scope.currentSubscriber.locomotion == "CAR"){
    		car = $scope.currentSubscriber;
    	}else if($scope.currentSubscriber && $scope.currentSubscriber.locomotion == "AUTOSTOP"){
    		if($scope.currentSubscriber.carRef){
    			car = $scope.refSubscribers[$scope.currentSubscriber.carRef];	
    		}
    	}
    	if(car){
			$scope.directionsDisplay.setMap($scope.myMap);
			var start = new google.maps.LatLng(car.address.location.lat, car.address.location.lng);
			var end = new google.maps.LatLng($scope.event.address.location.lat, $scope.event.address.location.lng);
			var waypts = [];
			if(car && car.car && car.car.passengers){
			  for (var i = 0; i < car.car.passengers.length; i++) {
				  var passenger = $scope.refSubscribers[car.car.passengers[i]];
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
    	}else{
    		$scope.directionsDisplay.setMap(null);
    		$scope.myMap.fitBounds($scope.bounds);
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
    function reloadSubscribers($scope){
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
    	if(subscriber.car && subscriber.car.links){
    		$scope.subscribersLinks[subscriber.userRef].carlinks = buildLinks(subscriber.car.links);	
    	}
    	
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
   			if($scope.currentSubscriber && $scope.currentSubscriber.locomotion=='CAR' && subscriber.locomotion=='AUTOSTOP'){
   				if(subscriber.carRef == $scope.currentSubscriber.userRef){
	   				subscriber.picto = $scope.eventLinks.pictoStopLight;
	   				subscriber.inMyCar = true;
   				}else if($scope.currentSubscriber.car && $scope.currentSubscriber.car.waitings && $scope.currentSubscriber.car.waitings.length>0 && inArray(subscriber.userRef, $scope.currentSubscriber.car.waitings)){
					subscriber.picto = $scope.eventLinks.pictoStop;
		   			subscriber.waitingForMyCar = true;
   				}else if(inArray($scope.currentSubscriber.userRef, subscriber.possibleCars)){
   					subscriber.picto = $scope.eventLinks.pictoStop;
   					subscriber.requestedByMe = true;
				}else{
	   				subscriber.picto = $scope.eventLinks.pictoStopDark;
					subscriber.free = true;
	   			}	
   			}else if($scope.currentSubscriber && $scope.currentSubscriber.locomotion=='AUTOSTOP' && subscriber.locomotion=='CAR'){
				if($scope.currentSubscriber.carRef == subscriber.userRef){
					subscriber.picto = $scope.eventLinks.pictoCarLight;
   					subscriber.currentCar = true;	
				}else if(subscriber.car && subscriber.car.waitings && subscriber.car.waitings.length>0 && 
						inArray($scope.currentSubscriber.userRef, subscriber.car.waitings)){
					subscriber.picto = $scope.eventLinks.pictoCar;
   					subscriber.iAskHim = true;
				}else if(subscriber.userRef &&  
						inArray(subscriber.userRef, $scope.currentSubscriber.possibleCars)){
					subscriber.picto = $scope.eventLinks.pictoCar;
   					subscriber.heAskMeToBeInHisCar = true;
				}else{
   					subscriber.picto = $scope.eventLinks.pictoCarDark;
   					subscriber.normalCar = true;
   				}
   			}else if(subscriber.locomotion=='CAR'){
   				subscriber.picto = $scope.eventLinks.pictoCarDark;
   			}else if(subscriber.locomotion=='AUTOSTOP'){
   				subscriber.picto = $scope.eventLinks.pictoStopDark;
   			}else{
   				subscriber.picto = $scope.eventLinks.pictoDontKnow;
   				subscriber.dontKnow = true;
   			}

   			addMarkerSubscriber(subscriber);
   		}
   		
   		if($scope.currentSubscriber && $scope.currentSubscriber.locomotion=='CAR'){
   			$scope.currentSubscriber.picto = $scope.eventLinks.pictoCarLight;	
   		}else if($scope.currentSubscriber && $scope.currentSubscriber.locomotion=='AUTOSTOP'){
   			$scope.currentSubscriber.picto = $scope.eventLinks.pictoStopLight;
   		}
   		
   		if(callback){
		   		for(var i=0; i<length; i++){
		   			var subscriber = subscribers[i];
		   			callback(subscriber);
		   		}
			}
	   	$scope.subscribers = subscribers;
	   	traceDirections($scope);
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
    function floatEqual(f1, f2) {
		return (Math.abs(f1 - f2) < 0.000001);
	}
}
//EventCtrl.$inject = ['$scope','register'];

