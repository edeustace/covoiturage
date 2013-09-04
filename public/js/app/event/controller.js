'use strict';





/* Controllers */

function EventCtrl($scope, $http, $location, $compile, $filter, mailUtils, mapService, eventService, chatService, $anchorScroll, $modal) {
	//////////////  ATTRIBUTS  ///////////////////
	$scope.items = [
	                  { id: "CAR", name: 'en voiture' },
	                  { id: "AUTOSTOP", name: 'à pied' },
	                  { id: "DONT_KNOW_YET", name: 'Je ne sais pas encore' }
                  ];
	$scope.editMode = false;
	$scope.alerts = [];
	$scope.eltsInChat = 10;
	$scope.eltsInMainChat = 10;
	$scope.opts = {
        backdropFade: true,
        dialogFade:true
      };

    $scope.editAddTopic = false;
    $scope.filter = {
        car : false,
        autostop : false
    }

	//////////////  SCOPE METHODS  ///////////////////
    $scope.setFilterToCar = function(){
        if($scope.filter && $scope.filter.car){
            $scope.filterUsers = null;
        }else{
            $scope.filter.autostop = false;
            $scope.filterUsers = 'CAR';
        }
    };
    $scope.setFilterToAutostop = function(){
        if($scope.filter && $scope.filter.autostop){
            $scope.filterUsers = null;
        }else{
            $scope.filter.car = false;
            $scope.filterUsers = 'AUTOSTOP'

        }
    };

	$scope.getMapOptions = function(){
	    return mapService.getMapOptions();
	};
    $scope.scrollTo = function(id) {
        $location.hash(id);
        $anchorScroll();
    };

	$scope.securise = function(){
		eventService.securise().then(function(data){
            $scope.event.contactsOnly = true;
		});
	};
	$scope.unSecurise = function(){
	    eventService.securise().then(function(data){
            $scope.event.contactsOnly = false;
    	})
	};
	$scope.subscribe = function(){
	    eventService.subscribe($scope.newSubscriber).then(
            function(data){
                $scope.alerts.push({type:"success", msg:"Vous participez à l'événement !"});
            },
            function(data){
                eventService.formatErrors(data, function(msg){
                    $scope.alerts.push({type:"error", msg:msg});
                });
	        });
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
                eventService.updateLocomotion(locomotion);
            }
        }
    };

	
	$scope.saveCurrentSubscriber = function(){
	    eventService.saveSubscriber($scope.currentSubscriber).then(
	        function(data){
               $scope.setEditMode(false);
            },
            function(data){
                eventService.formatErrors(data, function(msg){
                    $scope.alerts.push({type:"error", msg:msg});
                });
	        });
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
        var carRef = $scope.user.id;
        eventService.proposeSeat(passenger, carRef).then(function(subscriber){
            $scope.myInfoWindow.close();
        });
	};
    $scope.removeWaitingGuy = function(car, passenger){
        eventService.removeWaitingGuy(car, passenger).then(function(data){
            $scope.myInfoWindow.close();
        });
	};
	$scope.validatePassenger = function(car, passenger){
	    eventService.validatePassenger(car, passenger).then(function(subscriber){
	        $scope.myInfoWindow.close();
	    });
	};
	$scope.removePassenger = function(car, passenger){
        eventService.removePassenger(car, passenger).then(function(subscriber){
	        $scope.myInfoWindow.close();
	    });
	};
	//AUTOSTOPER 
	$scope.askForSeat = function(car){
        eventService.askForSeat(car).then(function(subscriber){
	        $scope.myInfoWindow.close();
	    });
	};
	$scope.removePossibleCar = function(passenger, carRef){
        eventService.removePossibleCar(passenger, carRef).then(function(subscriber){
	        $scope.myInfoWindow.close();
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
        $scope.myInfoWindow.open($scope.map, marker);
    };
    $scope.setToUpdated = function(){
      if(!$scope.event.updated && $scope.event){
           $scope.event.updated = true;
           $http.put(eventService.getEventLinks().self, $scope.event);
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
            $http.put(eventService.getEventLinks().self, $scope.event).success(function(){
                $scope.eventEdit = false;
                mapService.setMarkerEvent($scope.event, $scope);
            });
        }
    };

    // Ajouter des contacts à l'événement
    $scope.addContact = function(contacts){
        $scope.newContacts = mailUtils.pushMails(contacts, $scope.newContacts);
        //TODO créer un service dans chat
        $http.post(eventService.getEventLinks().contacts, {contacts:$scope.newContacts}).success(function(){
            for ( var int = 0; int < $scope.newContacts.length; int++) {
                var contact = $scope.newContacts[int];
                $scope.event.contacts.push(contact);
            }
            $scope.addedContact = null;
            $scope.newContacts = new Array();
        }).error(function(error){
            eventService.formatErrors(error, function(msg){
                $scope.alerts.push({type:"error", msg:msg});
            });
        });

    };
    $scope.remove = function(index){
        $scope.newContacts.splice(index, 1);
    };
    $scope.closeAlert = function(index) {
        if($scope.alerts[index].notification && $scope.alerts[index].notification.id){
            if($scope.currentSubscriber){
                //TODO notifications : créer un service
                var link = eventService.getSubscriberLinks($scope.currentSubscriber.userRef).notifications;
                $http.delete(link+'/'+$scope.alerts[index].notification.id);
            }
        }
        $scope.alerts.splice(index, 1);
    };
    $scope.getSubscriber = function(ref){
        return eventService.getSubscriber(ref);
    };

    $scope.getMarkers = function(){
        return mapService.getMarkers();
    };

    $scope.getMarker = function(i){
        return mapService.getMarker(i);
    };

    ///////////////  CHAT ////////////////////////////////

    $scope.createTopic = function(subscriber){
        chatService.createTopic($scope.idEvent, subscriber, eventService.getCurrentSubscriber()).then(function(){
            $scope.scrollTo('discussion');
        }, function(){
            console.log("Erreur à la creation d'un topic");
        });
    };
    $scope.createTopicForCar = function(){
        chatService.createTopicForCar($scope.idEvent, $scope.currentCar).then(function(){
            $scope.scrollTo('discussion');
        });
    };
    $scope.sendMessageToWall = function(currentMessage){
        chatService.sendMessageToWall(currentMessage, $scope.user.id, $scope.idEvent);
        $scope.mainChat.currentMessage = null;
    };

    $scope.sendMessage = function(currentMessage){
        chatService.sendMessage(currentMessage, $scope.user.id, $scope.idEvent);
        $scope.chat.currentMessage = null;
    };

    $scope.loadMainChatMessages = function(){
        chatService.loadMainChatMessages();
    };

    $scope.loadMessages = function(aTopic){
        chatService.loadMessages(aTopic);
    };

    //LISTENERS SUR LE SERVER SENT EVENT
    $scope.addTopic = function(msg){
        var topic = JSON.parse(msg.data);
        if(topic.type && topic.type=='topic'){
            chatService.addTopic(topic, $scope.user.id);
            $scope.$apply();
        }
    };

    $scope.addMessage = function(msg){
        var message = JSON.parse(msg.data);
        if(message.type=='message'){
            message = message.data;
            chatService.addMessage(message, $scope.user.id);
            $scope.$apply();
        }
    };

    $scope.addNotification = function(msg){
        var message= JSON.parse(msg.data);
        if(message.type == 'notification'){
            var notification = message.data;
            $scope.alerts.push({type:notification.type, msg:notification.message, notification:notification});
            eventService.reloadSubscribers($scope);
        }
    };

    $scope.handleSubscriberUpdated = function(msg){
        var notification = JSON.parse(msg.data);
        if(!notification.type == 'subscriber'){
            eventService.reloadSubscribers($scope);
        }
    };

    $scope.listen = function(currentSubscriber){
        var feed = eventService.getSubscriberLinks(currentSubscriber.userRef).feed;
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


    $scope.addToTopic = function(){
        chatService.addToTopic($scope.chat.currentTopic.newTopicSubscribers).then(
            function(){
                $scope.chat.currentTopic.addNewSubscribersStatut = 'RECEIVED';
            },
            function(){
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
    };

    ///////////////  INIT /////////////////////
    

    $scope.getSubscribers = function(){
        return eventService.getSubscribers();
    };

    eventService.addListenerOnEvent(function(event){
        $scope.event = event;
        if($scope.event.creator.id == $scope.user.id){
            $scope.showEventEditButton = true;
        }
        if(!$scope.event.updated && $scope.showEventEditButton){
            $scope.showUpdated = true;
            $scope.eventEdit = true;
        }
    });
    eventService.addListenerOnSubscribers(function(subscribers){
        $scope.subscribers = subscribers;
        mapService.fitBounds();
        if($scope.currentInfoWindowsSubscriber){
            var marker = mapService.findMarkerByUserRef($scope.currentInfoWindowsSubscriber.userRef);
            var subscriber = eventService.getSubscriber($scope.currentInfoWindowsSubscriber.userRef);
            if(marker && subscriber){
                $scope.currentMarker = marker;
                $scope.currentInfoWindowsSubscriber = subscriber;
            }
        }
    });

    eventService.addListenerOnCurrentSubscriber(function(current){
        $scope.currentSubscriber = current;
        if(!current){
            $scope.newSubscriber = {
                    userRef : $scope.user.id,
                    email : $scope.user.email,
                    name : $scope.user.name,
                    surname : $scope.user.surname,
                    locomotion : 'CAR'
            };
        }else{
            $scope.listen(current);
        }
    });
    eventService.addListenerOnCurrentCar(function(car){
        $scope.currentCar = car;
    });

    chatService.init($scope);

    var id = extractFromUrl($location.absUrl());
    $scope.idEvent = id;
    $http.get('/rest/users/current').success(function(user) {
		if(user){
			$scope.user = user;
			mapService.init($scope.map);

            chatService.loadTopics(id, user.id);
            chatService.loadWall(id, user.id);

			eventService.loadEvent(id, user.id, function(data){

                if(eventService.getSubscriberLinks(data.currentSubscriber.userRef).notifications){
                    $http.get(eventService.getSubscriberLinks(data.currentSubscriber.userRef).notifications).success(function(notifications) {
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
                    mapService.getMarkers(function(marker){
                        if(newValue){
                            if(!(marker.type === newValue) && (marker.type != "EVENT") && !marker.subscriber.current){
                                marker.setVisible(false);
                            }else{
                                marker.setVisible(true);
                            }
                        }else{
                            marker.setVisible(true);
                        }
                    });
                });
			});
		}
	}).error(function(error){
		eventService.formatErrors(error, function(msg){
           $scope.alerts.push({type:"error", msg:msg});
       });
	});
    
    /////////   PRIVATE   ////////////////

    function extractFromUrl(url){
        var index = url.lastIndexOf("/")
        var end = url.length;
        if(url.lastIndexOf("##")>index){
            end = url.lastIndexOf("##");
        }
        return url.substring(index+1, end);
    }

}
//EventCtrl.$inject = ['$scope','register'];

