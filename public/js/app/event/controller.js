'use strict';




/* Controllers */

function EventCtrl($scope, $http, $location, $compile, mailUtils, mapService, eventService) {
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
	$scope.securise = function(){
		$http.put($scope.eventLinks.securised, {value:true}).success(function(){
			$scope.event.contactsOnly = true;
		}).error(function(error){
			alert("Error "+error);
		});
	};
	$scope.unSecurise = function(){
		$http.put($scope.eventLinks.securised, {value:false}).success(function(){
			$scope.event.contactsOnly = false;
		}).error(function(error){
			alert("Error "+error);
		});
	};
	$scope.subscribe = function(){
		if($scope.newSubscriber && $scope.newSubscriber.userRef){
			$http.post($scope.eventLinks.subscribers, $scope.newSubscriber).success(function(data){
				$scope.alerts.push({type:"success", msg:"Vous participez à l'événement !"});			
				eventService.reloadSubscribers($scope);
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
		$scope.newContacts = mailUtils.pushMails($scope.addedContact, $scope.newContacts);
		$http.post($scope.eventLinks.contacts, {contacts:$scope.newContacts}).success(function(){
			for ( var int = 0; int < $scope.newContacts.length; int++) {
				var contact = $scope.newContacts[int];
				$scope.event.contacts.push(contact);
			}
			$scope.addedContact = null;
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
				eventService.saveSubscriber(currentSubscriber, $scope);
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
	    		eventService.reloadSubscribers($scope);
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
				eventService.reloadSubscribers($scope);
				$scope.myInfoWindow.close();
			}).error(function(error){
				alert("Error "+error);
			});
    	}
	};
	$scope.validatePassenger = function(car, passenger){
    	var link = $scope.subscribersLinks[car.userRef].car;
    	$http.post(link, {passenger:passenger}).success(function(subscriber){
    		eventService.reloadSubscribers($scope);
    		$scope.myInfoWindow.close();
		}).error(function(error){
			alert("Error "+error);
		});
	};
	$scope.removePassenger = function(car, passenger){
    	var link = $scope.subscribersLinks[car.userRef].car;
    	$http.delete(link+'/'+passenger).success(function(subscriber){
			eventService.reloadSubscribers($scope);
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
		    		eventService.reloadSubscribers($scope);
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
    		eventService.reloadSubscribers($scope);
    		$scope.myInfoWindow.close();
		}).error(function(error){
			alert("Error "+error);
		});
	};
    $scope.setEditMode = function(value){
    	$scope.editMode = value;	
    };
    
    $scope.openSubscriberInfo = function(subscriber) {
    	var marker = mapService.findMarkerByLatLng($scope.myMarkers, subscriber.address.location.lat, subscriber.address.location.lng);
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
			    	$scope.eventLinks = eventService.buildLinks(event.links);
			    	event.picto = $scope.eventLinks.pictoFinish;
				   	if(event){
				        mapService.addMarkerEvent(event, $scope);
					   	if(event.subscribers){
					   		eventService.initSubscribers($scope, event.subscribers, function(subscriber){
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
				    	eventService.reloadSubscribers($scope);
				    };
				   	
				    if($scope.currentSubscriber && $scope.subscribersLinks[$scope.currentSubscriber.userRef] && $scope.subscribersLinks[$scope.currentSubscriber.userRef].notifications){
					    $http.get($scope.subscribersLinks[$scope.currentSubscriber.userRef].notifications).success(function(notifications) {
					    	for ( var int = 0; int < notifications.length; int++) {
								var notification = notifications[int];
								if(notification.message){
								    $scope.alerts.push({type:notification.type, msg:notification.message, notification:notification});
								}
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

    function extractFromUrl(url){
        var index = url.lastIndexOf("/")
        return url.substring(index+1, url.length);
    }

}
//EventCtrl.$inject = ['$scope','register'];

