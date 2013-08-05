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
	$scope.mainChat = {
	    messages:new Array(),
	    topic :{}
	};
	$scope.eltsInChat = 10;
	$scope.eltsInMainChat = 10;
	$scope.opts = {
        backdropFade: true,
        dialogFade:true
      };

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
            categorie:'chat',
            subscribers:subscribers
        };
        var topicId = null;
        for(var i in $scope.topics){
            var currentTopic = $scope.topics[i];
            if(compareArrays(currentTopic.subscribers, subscribers)){
                topicId = currentTopic.id;
                break;
            }
        }
        if(!topicId){
            $http.post($scope.eventLinks.topics, topic);
        }else{
           setCurrentTopic(topicId);
        }
    };

    function compareArrays(array1, array2){
        if(array1.sort().join(',')=== array2.sort().join(',')){
            return true;
        }else{
            return false;
        }
    }

    function setCurrentTopic(id){
         for(var i in $scope.topics){
            var currentTopic = $scope.topics[i];
            if(currentTopic.id === id){
                currentTopic.active = true;
            }else{
                currentTopic.active = false;
            }
         }
    }

    $scope.sendMessageToMainChat = function(currentMessage){
        sendAMessage(currentMessage, $scope.mainChat.topic);
        $scope.mainChat.currentMessage = null;
    };

    $scope.sendMessage = function(currentMessage){

        sendAMessage(currentMessage, $scope.chat.currentTopic);

        if(!$scope.chat.messages){
           $scope.chat.messages = new Array();
        }
        //$scope.chat.messages.push(messageToSend);
        $scope.chat.currentMessage = null;
    };

    function sendAMessage(currentMessage, topic){
        if(topic.date){
            topic.date = new Date(topic.date);
        }
        topic.update = new Date();
        var messageToSend = {
            type:"message",
            topic: topic,
            from: $scope.currentSubscriber.userRef,
            message: currentMessage
        };
        $http.post($scope.eventLinks.topics+'/'+topic.id+'/messages', messageToSend);
    };

    $scope.openModalChat= function(aTopic){
        $scope.shouldBeOpen = true;
        $scope.loadMessages(aTopic);
    };

    $scope.loadMainChatMessages = function(){
        $http.get('/rest/messages/'+$scope.mainChat.topic.id).success(function(messages){
            $scope.mainChat.messages = messages;
            if(messages){
                for(var i in $scope.mainChat.messages){
                    if($scope.mainChat.messages[i].date){
                     $scope.mainChat.messages[i].date = new Date($scope.mainChat.messages[i].date);
                    }
                }
            }
            formatMessages();
        });
    };

    $scope.loadMessages = function(aTopic){
        if(aTopic){
            $scope.eltsInChat = 5;
            aTopic.alert = null;
            setCurrentTopic(aTopic.id);
            $http.get('/rest/messages/'+aTopic.id).success(function(messages){
                $scope.chat = {
                    messages : messages,
                    currentTopic : aTopic
                };
                formatMessages();
            });
        }
    };


    $scope.addTopic = function(msg){
        var topic = JSON.parse(msg.data);
        if(topic.type && topic.type=='topic'){
            var exist = false;
            topic.date = new Date(topic.date);
            topic.update = new Date(topic.update);
            if(topic.categorie=='mainChat'){
                $scope.mainChat.topic = topic;
            }else{
                topic.active = false;
                for(var i in $scope.topics){
                    if($scope.topics[i].id == topic.id){
                        exist = true;
                        $scope.topics[i].date = topic.date;
                    }
                }
                if(!exist){
                    $scope.topics.push(topic);
                    if(topic.creator === $scope.currentSubscriber.userRef){
                        $scope.loadMessages(topic);
                    }
                }
            }
            $scope.$apply();
        }
    };

    $scope.addMessage = function(msg){
        var message = JSON.parse(msg.data);
        if(message.type=='message'){
            if($scope.mainChat.topic && (message.topicRef == $scope.mainChat.topic.id)){
                message.date = new Date(message.date);
                $scope.mainChat.messages.push(message);
                $scope.$apply();
            }else if($scope.topics){
                for(var i in $scope.topics){
                    var topic = $scope.topics[i];
                    if(topic.id == message.topicRef){
                        topic.date = new Date(message.date);
                        topic.update = new Date(message.date);
                        if(topic.active){
                            message.date = new Date(message.date);
                            $scope.chat.messages.push(message);
                        }else{
                            if(topic.alert){
                                topic.alert++;
                            }else{
                                topic.alert = 1;
                            }
                        }
                        $scope.$apply();
                    }
                }
            }
        }
    };

    $scope.addNotification = function(msg){
        var notification = JSON.parse(msg.data);
        if(notification.type == 'notification'){
            $scope.alerts.push({type:notification.type, msg:notification.message, notification:notification});
            eventService.reloadSubscribers($scope);
        }
    };

    $scope.handleSubscriberUpdated = function(msg){
        var notification = JSON.parse(msg.data);
        if(!notification.type){
            eventService.reloadSubscribers($scope);
        }
    };

    $scope.listen = function(){
        var feed = $scope.subscribersLinks[$scope.currentSubscriber.userRef].feed;
        $scope.feed = new EventSource(feed);
        $scope.feed.addEventListener("open", function(msg){
            console.log(feed+' : sse open !');
        }, false);
        $scope.feed.addEventListener("error", function(e){
            if (e.readyState == EventSource.CLOSED) {
                console.log('connection close');
            }else{
                console.log(e);
            }
        }, false);
        $scope.feed.addEventListener("message", $scope.addTopic, false);
        $scope.feed.addEventListener("message", $scope.addMessage, false);
        $scope.feed.addEventListener("message", $scope.addNotification, false);
        $scope.feed.addEventListener("message", $scope.handleSubscriberUpdated, false);
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
					   			//var idUser = $scope.user.id;
								//if(idUser == subscriber.userRef){
								//	if(subscriber.locomotion=="CAR"){
								//		$scope.filterUsers = "AUTOSTOP";
								//	}else if(subscriber.locomotion=="AUTOSTOP"){
								//		$scope.filterUsers = "CAR";
								//	}
								//}
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



                    $http.get($scope.eventLinks.topics+'?user='+$scope.user.id).success(function(topics){
                        if(topics){
                            $scope.topics = new Array();
                            var maxDate = null;
                            var maxDateTopic = null;
                            for(var i in topics){
                                var topic = topics[i];
                                topic.active = false;
                                topic.title = $scope.refSubscribers[topic.creator].name;
                                topic.date = new Date(topic.date);
                                if((!maxDate) || (topic.update > maxDate)){
                                    maxDate=topic.update;
                                    maxDateTopic = topic;
                                }
                                topic.update = new Date(topic.update);
                                $scope.topics.push(topic);
                            }
                            $scope.loadMessages(maxDateTopic);
                        }
                    });

				   	$http.get($scope.eventLinks.topics+'?categorie=mainChat').success(function(topics){
                        if(topics && topics.length>0){
                            var topic = topics[0];
                            topic.date = new Date(topic.date);
                            topic.update = new Date(topic.update);
                            $scope.mainChat.topic = topic;
                            $scope.loadMainChatMessages();
                        }else{
                            var topic = {
                                idEvent:$scope.event.id,
                                creator:'event',
                                type:'topic',
                                categorie:'mainChat',
                                subscribers:new Array()
                            };
                            $http.post($scope.eventLinks.topics, topic).success(function(topic){
                                $scope.mainChat.topic = topic;
                            });
                        }
				   	});

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

                    //Connection to server sent events
                    $scope.listen();
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

