'use strict';





/* Controllers */

function EventCtrl($scope, $http, $location, $compile, $filter, mailUtils, mapService, eventService) {
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
	$scope.topics = new Array();
	$scope.chat = {messages:new Array()};
	$scope.eltsInChat = 5;
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
	$scope.addContact = function(contacts){
		$scope.newContacts = mailUtils.pushMails(contacts, $scope.newContacts);
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
				eventService.saveSubscriber($scope.currentSubscriber, $scope);
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
        //$scope.$apply();
    };
    $scope.setToUpdated = function(){
      if(!$scope.event.updated && $scope.event){
           $scope.event.updated = true;
           $http.put($scope.eventLinks.self, $scope.event);
      }
    };
    $scope.cancelUpdate = function(){
        $scope.eventEdit = false;
        $scope.setToUpdated();
    };
    $scope.setEventEditMode = function(value){
        $scope.eventEdit = value;
    };
    $scope.saveEvent = function(){
        if($scope.event){
            $scope.event.updated = true;
            $http.put($scope.eventLinks.self, $scope.event).success(function(){
                $scope.eventEdit = false;
                mapService.setMarkerEvent($scope.event, $scope);
            });
        }
    };

    $scope.createTopicTitle=function(topic){
      var title = $scope.subscribersRef[topic.creator].name;
      return   title;
    };

    $scope.createTopic = function(subscriber){
        var subscribers = new Array();
        subscribers.push(subscriber.userRef);
        subscribers.push($scope.currentSubscriber.userRef);

        var topic = {
                idEvent:$scope.event.id,
                type:"topic",
                creator:$scope.currentSubscriber.userRef,
                subscribers:subscribers
            };

        var existing = ($scope.topics.length>0);
        if(existing){
            for(var i in $scope.topics){
                var currentTopic = $scope.topics[i];
                if(currentTopic.subscribers.length == 2){
                    existing = true;
                    for (var j in currentTopic.subscribers){
                        var sub = currentTopic.subscribers[j];
                        existing = (existing && ((sub === subscriber.userRef) || (sub === $scope.currentSubscriber.userRef)));
                    }
                    if(existing){
                        break;
                    }
                }
            }
        }
        if(!existing){
            $scope.webSocket.send(JSON.stringify(topic));
            topic.title = $scope.refSubscribers[topic.creator].name;
            $scope.topics.push(topic);
        }
    };

    $scope.sendMessage = function(topic, currentMessage){
        if(topic.date){
            topic.date = new Date(topic.date);
        }

        var messageToSend = {
            type:"message",
            topic: topic,
            from: $scope.currentSubscriber.userRef,
            message: currentMessage
        };
        $scope.webSocket.send(JSON.stringify(messageToSend));
        if(!$scope.chat.messages){
           $scope.chat.messages = new Array();
        }
        //$scope.chat.messages.push(messageToSend);
        $scope.chat.currentMessage = null;
    };


    $scope.loadMessages = function(aTopic){
        $scope.eltsInChat = 5;
        aTopic.alert = null;
        $http.get('/rest/messages/'+aTopic.id).success(function(messages){
            $scope.chat.messages = messages;
            formatMessages();
        });
    };

    function formatMessages(){
         if($scope.chat.messages){
             for(var i in $scope.chat.messages){
                 if($scope.chat.messages[i].date){
                     $scope.chat.messages[i].date = new Date($scope.chat.messages[i].date);
                 }
             }
         }
    }

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
				   	    if(event.fromDate){
                            event.fromDate = new Date(event.fromDate);
				   	    }
				   	    if(event.creator.id == user.id){
				   	        $scope.showEventEditButton = true;
				   	    }
				   	    if(!event.updated && $scope.showEventEditButton){
				   	        $scope.showUpdated = true;
                            $scope.eventEdit = true;
				   	    }


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

                    $http.get('/rest/topics/'+$scope.event.id+'/users/'+$scope.user.id).success(function(topics){
                       if(topics){
                        $scope.topics = new Array();
                        for(var i in topics){
                            var topic = topics[i];
                            topic.title = $scope.refSubscribers[topic.creator].name;
                            $scope.topics.push(topic);
                        }
                        if($scope.topics.length>0){
                            $scope.topics[0].active = true;
                            $http.get('/rest/messages/'+$scope.topics[0].id).success(function(messages){
                                $scope.chat.messages = messages;
                                formatMessages();
                            });

                        }
                       }
                    });

                    var wsUrl = jsRoutes.controllers.SubscriberCtrl.subscribersUpdates(event.id, $scope.user.id);
                    var ws = new WebSocket(wsUrl.webSocketURL());
                    //var ws = new SockJS(wsUrl.webSocketURL());
                    ws.onopen = function() {
                        console.log('Web socket open');
                    };
                    ws.onmessage = function(wsMsg) {
                        if(wsMsg && wsMsg.data){
                            var notification = JSON.parse(wsMsg.data);
                            if(notification.type){
                                if(notification.type=='topic'){
                                    var exist = false;
                                    for(var i in $scope.topics){
                                        if($scope.topics[i].id = notification.id){
                                            exist = true;
                                        }
                                    }
                                    if(!exist){
                                        $scope.topics.push(notification);
                                    }
                                }else if(notification.type=='message'){
                                    if($scope.topics){
                                        for(var i in $scope.topics){
                                            if($scope.topics[i].id == notification.topicRef){
                                                if($scope.topics[i].active){
                                                    notification.date = new Date(notification.date);
                                                    $scope.chat.messages.push(notification);
                                                }else{
                                                    if($scope.topics[i].alert){
                                                        $scope.topics[i].alert++;
                                                    }else{
                                                        $scope.topics[i].alert = 1
                                                    }
                                                }
                                                $scope.$apply();
                                            }
                                        }
                                    }
                                }else if(notification.message){
                                    $scope.alerts.push({type:notification.type, msg:notification.message, notification:notification});
                                    eventService.reloadSubscribers($scope);
                                }
                            }else{
                                eventService.reloadSubscribers($scope);
                            }

                        }

                    };
                    $scope.webSocket = ws;

				   	
				    if($scope.currentSubscriber && $scope.subscribersLinks[$scope.currentSubscriber.userRef] && $scope.subscribersLinks[$scope.currentSubscriber.userRef].notifications){
					    $http.get($scope.subscribersLinks[$scope.currentSubscriber.userRef].notifications).success(function(notifications) {
					    	for ( var int = 0; int < notifications.length; int++) {
								var notification = notifications[int];
								if(notification.message){
								    var msg = notification.message;
								    if(notification.date){
                                        msg = 'le '+new Date(notification.date) +' : '+notification.message;
								    }
								    $scope.alerts.push({type:notification.type, msg:msg, notification:notification});
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

