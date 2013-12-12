


angular.module('chatModule', [], function($provide){

    $provide.factory('chatService', ['$http', '$q', function($http, $q) {

        function generateId(){
            return Math.floor((Math.random()*1000)+1);
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
            for(var i in $data.chat.topics){
                var currentTopic = $data.chat.topics[i];
                if(currentTopic.id === id){
                    currentTopic.active = true;
                    $data.chat.currentTopic = currentTopic;
                }else{
                    currentTopic.active = false;
                }
            }

        }
        function removeTopic(topic){
            var id = topic.id;
            var indice;
            for(var i in $data.chat.topics){
                var currentTopic=$data.chat.topics[i];
                 if(currentTopic.id && topic.id && currentTopic.id == topic.id){
                    indice = i;
                    break;
                 }
            }
            if(indice){
                $data.chat.topics.splice(indice, 1);
                if($data.chat.currentTopic && $data.chat.currentTopic.id == id){
                    $data.chat.currentTopic = null;
                    $data.chat.messages = new Array();
                }
            }

        }
        function addTopicToList(topic, topics, idCurrentUser, statut){
            if(topics){
                var found = false;
                for(var i in topics){
                    var currentTopic=topics[i];
                    if(statut=='UPDATED' || statut=='CREATED'){
                        if((currentTopic.id && topic.id && currentTopic.id == topic.id) || ((!currentTopic.id || !topic.id) && currentTopic.tmpId && topic.tmpId && currentTopic.tmpId == topic.tmpId)){
                            found = true;
                            var active = currentTopic.active;
                            topics[i] = topic;
                            if(active){
                                setCurrentTopic(topic.id);
                            }
                            topics[i].statut = 'RECEIVED';
                        }
                    }
                }
                if(!found){
                    if(topics.length == 0){
                        topic.active = true;
                    }
                    topics.push(topic);
                    if(topic.creator === idCurrentUser || topic.active){
                        $chatService.loadMessages(topic);
                    }
                }
            }
        }
        function topicExists(topic){
            if(topic){
                if(topic.categorie==='carChat'){
                    for(var i in $data.chat.topics){
                        var currentTopic = $data.chat.topics[i];
                        if(currentTopic.categorie=='carChat'){
                            return currentTopic;
                        }
                    }
                }else{
                    for(var i in $data.chat.topics){
                        var currentTopic = $data.chat.topics[i];
                        if(compareArrays(currentTopic.subscribers, topic.subscribers)){
                            return currentTopic;
                        }
                    }
                }
            }
            return null;
        }

        function createTopic(topic, idEvent){
            var deferred = $q.defer();
            var aTopic = topicExists(topic);

            if(!aTopic){
                var tmpIdTopic = generateId();
                topic.tmpId = tmpIdTopic;
                if(!$data.chat.topics){
                    $data.chat.topics = new Array();
                }
                pushTopic(topic);
                var url = jsRoutes.controllers.ChatCtrl.createTopic(idEvent);
                $http.post(url.url, topic).success(function(aTopic){
                    $chatService.loadMessages(aTopic);
                    deferred.resolve(aTopic);
                }).error(function(){
                    deferred.reject(aTopic);
                });
            }else if(aTopic.subscribers.length != topic.subscribers.length){
                aTopic.subscribers = topic.subscribers;
                var url = jsRoutes.controllers.ChatCtrl.updateTopic(idEvent, aTopic.id);
                $http.put(url.url, topic).success(function(aTopic){
                    $chatService.loadMessages(aTopic);
                    deferred.resolve(aTopic);
                }).error(function(){
                    deferred.reject(aTopic);
                });
            }else{
                $chatService.loadMessages(aTopic);
                deferred.resolve(aTopic);
            }
            return deferred.promise;
        }
        function addMessageToTopic(message, messages, idCurrentUser){
            var found = false;
            if(message.from == idCurrentUser || message.topic.categorie == 'wall'){
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
        function formatMessages(){
             if($data.chat.messages){
                 for(var i in $data.chat.messages){
                     if($data.chat.messages[i].date){
                         $data.chat.messages[i].date = new Date($data.chat.messages[i].date);
                     }
                 }
             }
        }
        function sendAMessage(currentMessage, topic, idCurrentUser, idEvent){
            if(topic.date){
                topic.date = new Date(topic.date);
            }
            topic.update = new Date();
            var tmpIdMessage = generateId();
            var messageToSend = {
                type:"message",
                topic: topic,
                tmpId: tmpIdMessage,
                from: idCurrentUser,
                message: currentMessage,
                date : new Date(),
                statut:"SENDING"
            };
            if(topic.categorie == "chat" || topic.categorie == "carChat"){
                addMessage(messageToSend);
            }else if(topic.categorie == "wall"){
                addMessageToWall(messageToSend);
            }
            var url = jsRoutes.controllers.ChatCtrl.createMessage(idEvent, topic.id);
            $http.post(url.url, messageToSend).success(function(message){
                if(topic.categorie == "chat" || topic.categorie == "carChat"){
                    addMessageToTopic(message, $data.chat.messages, idCurrentUser);
                }else if(topic.categorie == "wall"){
                    addMessageToTopic(message, $data.wall.messages);
                }
            }).error(function(){
                if(topic.categorie == "chat" || topic.categorie == "carChat"){
                    for(var i in $data.chat.messages){
                        if(!$data.chat.messages[i].id && $data.chat.messages[i].tmpId == tmpIdMessage){
                            $data.chat.messages[i].statut = "FAILURE";
                        }
                    }
                }else if(topic.categorie == "wall"){
                    for(var i in $data.wall.messages){
                        if(!$data.wall.messages[i].id && $data.wall.messages[i].tmpId == tmpIdMessage){
                            $data.wall.messages[i].statut = "FAILURE";
                        }
                    }
                }
            });
        }
        function addMessage(message){
            $data.chat.messages.push(message);
            for(var i in $onMessageAddedListeners){
                $onMessageAddedListeners[i](message);
            }
        }
        function addMessageToWall(message){
            $data.wall.messages.push(message);
            for(var i in $onMessageAddedListeners){
                $onMessageAddedOnWallListeners[i](message);
            }
        }
        function pushTopic(topic){
            $data.chat.topics.push(topic);
            for(var i in $onMessageAddedListeners){
                $onTopicAddedListeners[i](message);
            }
        }
        function inArray(value, array){
            if(array && value){
                for(var i in array){
                    if(value==array[i]){
                        return true;
                    }
                }
            }
            return false;
        }

        var $data = {
            chat : {
                messages : new Array(),
                topics : new Array(),
                currentTopic : null
            },
            wall : {
                messages:new Array(),
                topic :{}
            }
        };

        var $onMessageAddedListeners = new Array();
        var $onTopicAddedListeners = new Array();
        var $onCurrentTopicChangeListeners = new Array();
        var $onMessageAddedOnWallListeners = new Array();

        $chatService = {
            init : function($scope, template){
                $data = $scope;
                $data.chat = {
                    messages : new Array(),
                    topics : new Array(),
                    template : template,
                    currentTopic : null
                };
                $data.wall = {
                    messages:new Array(),
                    topic :{}
                };
            },
            addListenerOnMessageAdded : function(callback){
                $onMessageAddedListeners.push(callback);
            },
            addListenerOnTopicAddedListeners : function(callback){
                $onTopicAddedListeners.push(callback);
            },
            addListenerOnCurrentTopicChangeListeners : function(callback){
                $onCurrentTopicChangeListeners.push(callback);
            },
            addListenerOnMessageAddedOnWallListeners : function(callback){
                $onMessageAddedOnWallListeners.push(callback);
            },
            pushTopic : function(topic){
                pushTopic(topic);
            },
            loadTopics :  function(eventId, currentUserId){
                var deferred = $q.defer();
                if(eventId && currentUserId){
                    var url = jsRoutes.controllers.ChatCtrl.getTopics(eventId, null, currentUserId);
                    $http.get(url.url).success(function(topics){
                        if(topics){
                            $data.chat.topics = new Array();
                            var maxDate = null;
                            var maxDateTopic = null;
                            for(var i in topics){
                                var topic = topics[i];
                                topic.active = false;
                                topic.date = new Date(topic.date);
                                if((!maxDate) || (topic.update > maxDate)){
                                    maxDate=topic.update;
                                    maxDateTopic = topic;
                                }
                                topic.update = new Date(topic.update);
                                pushTopic(topic);
                            }
                            $chatService.loadMessages(maxDateTopic);
                        }
                        deferred.resolve(topics);
                    }).error(function(error){
                        deferred.reject(error);
                    });
                }else{
                    deferred.reject();
                }
                return deferred.promise;
            },
            loadWall : function(eventId){
                var url = jsRoutes.controllers.ChatCtrl.getTopics(eventId, 'wall', null);
                $http.get(url.url).success(function(topics){
                    if(topics && topics.length>0){
                        var topic = topics[0];
                        topic.date = new Date(topic.date);
                        topic.update = new Date(topic.update);
                        $data.wall.topic = topic;
                        $chatService.loadWallMessages();
                    }else{
                        var topic = {
                            idEvent:eventId,
                            creator:'event',
                            type:'topic',
                            categorie:'wall',
                            subscribers:new Array()
                        };
                        var url = jsRoutes.controllers.ChatCtrl.createTopic(eventId);
                        $http.post(url.url, topic).success(function(topic){
                            $data.wall.topic = topic;
                        });
                    }
                });
	        },
            createTopic : function(idEvent, subscriber, currentSubscriber){
                if(idEvent && subscriber && currentSubscriber){
                    var subscribers = new Array();
                    subscribers.push(subscriber.userRef);
                    subscribers.push(currentSubscriber.userRef);

                    var topic = {
                        idEvent:idEvent,
                        type:"topic",
                        creator:currentSubscriber.userRef,
                        categorie:'chat',
                        statut : 'SENDING',
                        subscribers:subscribers
                    };
                    return createTopic(topic, idEvent);
                }else{
                    var deferred = $q.defer();
                    deferred.reject();
                    return deferred.promise;
                }

            },
            createTopicForCar : function(idEvent, currentCar){
                if(currentCar && currentCar.driver){
                    var subscribers = new Array();
                    subscribers.push(currentCar.driver.id);
                    for(var i in currentCar.passengers){
                        subscribers.push(currentCar.passengers[i].id);
                    }
                    var topic = {
                        idEvent:idEvent,
                        type:"topic",
                        creator:currentCar.driver.id,
                        categorie:'carChat',
                        statut : 'SENDING',
                        subscribers:subscribers
                    };
                    return createTopic(topic, idEvent);
                }else{
                    var deferred = $q.defer();
                    deferred.reject();
                    return deferred.promise;
                }
            },
            sendMessageToWall : function(currentMessage, idCurrentUser, idEvent){
                sendAMessage(currentMessage, $data.wall.topic, idCurrentUser, idEvent);
            },
            sendMessage : function(currentMessage, idCurrentUser, idEvent){
                sendAMessage(currentMessage, $data.chat.currentTopic, idCurrentUser, idEvent);
            },

            loadWallMessages : function(){
                if($data.wall.topic.id){
                    var url = jsRoutes.controllers.ChatCtrl.getMessages($data.wall.topic.id);
                    $http.get(url.url).success(function(messages){
                        $data.wall.messages = messages;
                        if(messages){
                            for(var i in $data.wall.messages){
                                if($data.wall.messages[i].date){
                                    $data.wall.messages[i].date = new Date($data.wall.messages[i].date);
                                }
                            }
                        }
                        formatMessages();
                    });
                }
            },
            loadMessages : function(aTopic){
                if(aTopic && aTopic.id){
                    $data.eltsInChat = 10;
                    aTopic.alert = null;
                    setCurrentTopic(aTopic.id);
                    var url = jsRoutes.controllers.ChatCtrl.getMessages(aTopic.id);
                    $http.get(url.url).success(function(messages){
                        $data.chat.messages = messages;
                        formatMessages();
                    });
                }
            },

            addTopic : function(message, idUser){
                if(message && message.data){
                    var topic = message.data;
                    if(message.statut == 'DELETED'){
                        removeTopic(topic);
                    }else if(message.statut == 'UPDATED' && !inArray(idUser, topic.subscribers)){
                        removeTopic(topic);
                    }else{
                        var exist = false;
                        topic.date = new Date(topic.date);
                        topic.update = new Date(topic.update);
                        if(topic.categorie=='wall'){
                            $data.wall.topic = topic;
                        }else{
                            topic.active = false;
                            addTopicToList(topic, $data.chat.topics, idUser, message.statut);
                        }
                    }
                }
            },

            addMessage : function(message, idCurrentUser){
                if(message){
                    if($data.wall.topic && (message.topicRef == $data.wall.topic.id)){
                        message.date = new Date(message.date);
                        addMessageToTopic(message, $data.wall.messages);
                    }else if($data.chat.topics){
                        for(var i in $data.chat.topics){
                            var topic = $data.chat.topics[i];
                            if(topic.id == message.topicRef){
                                topic.date = new Date(message.date);
                                topic.update = new Date(message.date);
                                if(topic.active){
                                    message.date = new Date(message.date);
                                    addMessageToTopic(message, $data.chat.messages, idCurrentUser);
                                }else{
                                    if(topic.alert){
                                        topic.alert++;
                                    }else{
                                        topic.alert = 1;
                                    }
                                }
                                $data.chat.newMessage = true;
                            }
                        }
                    }
                }
            },
            addToTopic : function(idEvent, newTopicSubscribers){
                var deferred = $q.defer();
                var url = jsRoutes.controllers.ChatCtrl.addSubscribers(idEvent, $data.chat.currentTopic.id);
                $http.put(url.url, {subscribers:newTopicSubscribers}).success(function(data){
                    deferred.resolve(data);
                }).error(function(data){
                    deferred.reject(data);
                });
                return deferred.promise;
            },
            addNewSubscribersToTopic : function(newValue){
                if(!$data.chat.currentTopic.newTopicSubscribers){
                    $data.chat.currentTopic.newTopicSubscribers = new Array();
                }
                $data.chat.currentTopic.newTopicSubscribers.push(newValue);
                $data.chat.currentTopic.userToAddToTopic = null;
            }
        };

        return $chatService;

    }]);
});