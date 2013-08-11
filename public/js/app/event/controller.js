'use strict';





/* Controllers */

function EventCtrl($scope, $http, $location, $compile, $filter, mailUtils, mapService, eventService,  $anchorScroll, $modal) {
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
    $scope.editAddTopic = false;


	//////////////  SCOPE METHODS  ///////////////////	
    $scope.scrollTo = function(id) {
        $location.hash(id);
        $anchorScroll();
    };

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


    $scope.opts = {
        backdropFade: true,
        dialogFade:true
    };
	$scope.closeLocomotionPopin = function(){
        $scope.validationLocomotion = false;
	};

    $scope.updateLocomotion = function(locomotion){
        if($scope.currentSubscriber){
            if(locomotion=='AUTOSTOP' && $scope.currentSubscriber.locomotion == 'CAR' && !$scope.validationLocomotion){
                $scope.validationLocomotion = true;
                var modal = $modal({
                  template: 'popin.html',
                  show: true,
                  backdrop: 'static',
                  scope: $scope
                });
            }else{
                $scope.validationLocomotion = false;
                var link = $scope.subscribersLinks[$scope.currentSubscriber.userRef].locomotion;
                $http.put(link, {locomotion : locomotion}).success(function(){
                    eventService.reloadSubscribers($scope);
                });
            }
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
		if(marker.type == 'EVENT'){
            $scope.currentInfoWindowsEvent = marker;
            $scope.currentInfoWindowsSubscriber = null;
		}else {
		    $scope.currentInfoWindowsSubscriber = marker.subscriber;
		    $scope.currentInfoWindowsEvent = null;
		}

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
            statut : 'SENDING',
            subscribers:subscribers
        };

        createTopic(topic);
    };

    $scope.createTopicForCar = function(){
        if($scope.currentCar){
            var subscribers = new Array();
            subscribers.push($scope.currentCar.driver.id);
            for(var i in $scope.currentCar.passengers){
                subscribers.push($scope.currentCar.passengers[i].id);
            }
            var topic = {
                idEvent:$scope.event.id,
                type:"topic",
                creator:$scope.currentCar.driver.id,
                categorie:'carChat',
                statut : 'SENDING',
                subscribers:subscribers
            };
            createTopic(topic);
        }
    };

    function createTopic(topic){
        var aTopic = null;
        for(var i in $scope.topics){
            var currentTopic = $scope.topics[i];
            if(compareArrays(currentTopic.subscribers, topic.subscribers)){
                aTopic = currentTopic;
                break;
            }
        }
        if(!aTopic){
            var tmpIdTopic = getTmpIdTopic();
            topic.tmpId = tmpIdTopic;
            $scope.topics.push(topic);
            $scope.loadMessages(topic);
            $http.post($scope.eventLinks.topics, topic).success(function(aTopic){
                addTopicToList(aTopic, $scope.topics);
            }).error(function(){
                for(var i in $scope.topics){
                    if(!$scope.topics[i].id && topic.tmpId == $scope.topics[i].tmpId){
                        topics[i].statut = 'FAILURE';
                    }
                }
            });
        }else{
           $scope.loadMessages(aTopic);
        }
        $scope.scrollTo('discussion');
    }

    function addTopicToList(topic, topics){
        if(topics){
            var found = false;
            for(var i in topics){
                var currentTopic=topics[i];
                if((currentTopic.id && topic.id && currentTopic.id == topic.id) || ((!currentTopic.id || !topic.id) && currentTopic.tmpId && topic.tmpId && currentTopic.tmpId == topic.tmpId)){
                    found = true;
                    currentTopic.id = topic.id;
                    currentTopic.date = topic.date;
                    currentTopic.update = topic.update;
                    currentTopic.statut = 'RECEIVED';
                    currentTopic.subscribers = topic.subscribers;
                }
            }
            if(!found){
                if(topics.length == 0){
                    topic.active;
                }
                topics.push(topic);
                if(topic.creator === $scope.currentSubscriber.userRef || topic.active){
                    $scope.loadMessages(topic);
                }
            }
        }
    }

    var _tmpIdTopic = 0;
    function getTmpIdTopic(){
        _tmpIdTopic++;
        return _tmpIdTopic;
    }

    function compareArrays(array1, array2){
        if(!array1 || !array2){
            return false;
        }
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
        $scope.chat.currentMessage = null;
    };

    function sendAMessage(currentMessage, topic){
        if(topic.date){
            topic.date = new Date(topic.date);
        }
        topic.update = new Date();
        var tmpIdMessage = getTmpIdMessage();
        var messageToSend = {
            type:"message",
            topic: topic,
            tmpId: tmpIdMessage,
            from: $scope.currentSubscriber.userRef,
            message: currentMessage,
            date : new Date(),
            statut:"SENDING"
        };
        if(topic.categorie == "chat" || topic.categorie == "carChat"){
            $scope.chat.messages.push(messageToSend);
        }else if(topic.categorie == "mainChat"){
            $scope.mainChat.messages.push(messageToSend);
        }
        $http.post($scope.eventLinks.topics+'/'+topic.id+'/messages', messageToSend).success(function(message){
            if(topic.categorie == "chat" || topic.categorie == "carChat"){
                addMessageToTopic(message, $scope.chat.messages);
            }else if(topic.categorie == "mainChat"){
                addMessageToTopic(message, $scope.mainChat.messages);
            }
        }).error(function(){
            if(topic.categorie == "chat" || topic.categorie == "carChat"){
                for(var i in $scope.chat.messages){
                    if(!$scope.chat.messages[i].id && $scope.chat.messages[i].tmpId == tmpIdMessage){
                        $scope.chat.messages[i].statut = "FAILURE";
                    }
                }
            }else if(topic.categorie == "mainChat"){
                for(var i in $scope.mainChat.messages){
                    if(!$scope.mainChat.messages[i].id && $scope.mainChat.messages[i].tmpId == tmpIdMessage){
                        $scope.mainChat.messages[i].statut = "FAILURE";
                    }
                }
            }
        });
    };

    var _tmpIdMessage = 0;
    function getTmpIdMessage(){
        _tmpIdMessage++;
        return _tmpIdMessage;
    }

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
            $scope.eltsInChat = 10;
            aTopic.alert = null;
            setCurrentTopic(aTopic.id);
            $scope.chat.currentTopic = aTopic;
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
                addTopicToList(topic, $scope.topics);
            }
            $scope.$apply();
        }
    };

    $scope.addMessage = function(msg){
        var message = JSON.parse(msg.data);
        if(message.type=='message'){
            if($scope.mainChat.topic && (message.topicRef == $scope.mainChat.topic.id)){
                message.date = new Date(message.date);
                addMessageToTopic(message, $scope.mainChat.messages);
                $scope.$apply();
            }else if($scope.topics){
                for(var i in $scope.topics){
                    var topic = $scope.topics[i];
                    if(topic.id == message.topicRef){
                        topic.date = new Date(message.date);
                        topic.update = new Date(message.date);
                        if(topic.active){
                            message.date = new Date(message.date);
                            addMessageToTopic(message, $scope.chat.messages);
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

    function addMessageToTopic(message, messages){
        var found = false;
        if(message.from == $scope.currentSubscriber.userRef || message.topic.categorie == 'mainChat'){
            for(var i in messages){
                var msg = messages[i];
                if((msg.id && message.id && msg.id == message.id) || ((!msg.id || !message.id) && msg.tmpId && message.tmpId && msg.tmpId == message.tmpId)){
                    msg.statut = 'RECEIVED';
                    if(!msg.id){
                        msg.id = message.id;
                    }
                    if(!msg.date){
                        msg.date = message.date;
                    }
                    found = true;
                }
            }
        }
        if(!found){
            message.statut = 'RECEIVED';
            messages.push(message);
        }
    }

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

    $scope.nextMessages = function(){
        alert("next");
        $scope.eltsInChat += 5;
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

    $scope.addToTopic = function(){
        $http.put($scope.eventLinks.topics+'/'+$scope.chat.currentTopic.id+'/subscribers', {subscribers:$scope.chat.currentTopic.newTopicSubscribers}).success(function(){
            $scope.chat.currentTopic.addNewSubscribersStatut = 'RECEIVED';
        }).error(function(){
            $scope.chat.currentTopic.addNewSubscribersStatut = 'FAILURE';
        });
        $scope.setEditAddTopic(false);
        $scope.chat.currentTopic.addNewSubscribersStatut = 'SENDING';
    };

    var validator = new RegExp("^\\w{20,}$");
    $scope.$watch('chat.currentTopic.userToAddToTopic', function(newValue, oldValue) {
        if(newValue && validator.test(newValue)){
            if(!$scope.chat.currentTopic.newTopicSubscribers){
                $scope.chat.currentTopic.newTopicSubscribers = new Array();
            }
            $scope.chat.currentTopic.newTopicSubscribers.push(newValue);
            $scope.chat.currentTopic.userToAddToTopic = null;
        }
    });

    $scope.removeNewTopicSubscriber = function(index){
        $scope.chat.currentTopic.newTopicSubscribers.splice(index, 1);
    };
    $scope.setEditAddTopic = function(value){
        if(!value){
            $scope.userToAddToTopic = null;
            $scope.chat.currentTopic.newTopicSubscribers = new Array();
        }
        $scope.editAddTopic = value;
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

