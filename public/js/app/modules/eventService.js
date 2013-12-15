

//var eventModule = angular.module('eventModule', []);
//eventModule.factory('eventService', function(mapService, $http, $q) {

angular.module('eventModule', [], function($provide){

   $provide.factory('eventService', ['mapService', '$http', '$q', function(mapService, $http, $q) {

        function inArray(value, array){
            if(array && value){
                for ( var i = 0; i < array.length; i++) {
                    var arrayValue = array[i];
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
        function buildCarObject(carOwner){
            if(carOwner && carOwner.car){
                var car = {
                    driver :
                    {
                        name : carOwner.surname + ' ' + carOwner.name,
                        id : carOwner.userRef,
                        subscriber : carOwner
                    },
                    passengers : new Array()
                };

                for(var i in carOwner.car.passengers){
                    var ref = carOwner.car.passengers[i];
                    var passenger = $service.getSubscriber(ref);
                    car.passengers.push(
                    {
                        name:passenger.surname+' '+passenger.name,
                        id : passenger.userRef,
                        subscriber :passenger,
                        deleteRight : (carOwner.userRef == $event.currentSubscriber.userRef || $event.currentSubscriber.userRef == ref)
                    });
                }
                return car;
            }
            return null;
        }


        var $event = {
            links : {
                subscribers : {},
                event : {}
            },
            event :{},
            currentUser : {},
            currentSubscriber:{},
            subscribers : new Array(),
            currentCar : null,
        };
        var $refSubscribers = {};
        var $listenersOnSubscribers = new Array();
        var $listenersOnCurrentSubscriber = new Array();
        var $listenersOnEvent = new Array();
        var $listenersOnCurrentCar = new Array();

        var $service = {
            addListenerOnSubscribers : function(callback){
                $listenersOnSubscribers.push(callback);
            },
            addListenerOnCurrentSubscriber : function(callback){
                $listenersOnCurrentSubscriber.push(callback);
            },
            addListenerOnEvent : function(callback){
                $listenersOnEvent.push(callback);
            },
            addListenerOnCurrentCar : function(callback){
                $listenersOnCurrentCar.push(callback);
            },
            setSubscriber : function(subscriber){
                if(subscriber && subscriber.userRef){
                    $refSubscribers[subscriber.userRef] = subscriber;
                }
            },
            getCurrentSubscriber : function(){
                return $event.currentSubscriber;
            },
            getSubscriber : function(id){
                var subscriber = $refSubscribers[id];
                if(subscriber){
                    return subscriber
                }
                return null;
            },
            getEventLinks : function(){
                if($event.links.event){
                    return $event.links.event;
                }else{
                    return {};
                }
            },
            getSubscriberLinks : function(ref){
                if(ref){
                    var links = $event.links.subscribers[ref];
                    if(links){
                        return links
                    }else{
                        return {};
                    }
                }
                return {};
            },
            setCurrentSubscriber : function(value){
                if(value){
                    $event.currentSubscriber = value;
                    $event.currentSubscriber.visible=true;
                    $event.currentSubscriber.current = true;
                    for(var j in $listenersOnCurrentSubscriber){
                        $listenersOnCurrentSubscriber[j]($event.currentSubscriber);
                    }
                }else{
                    $event.currentSubscriber = value;
                    for(var j in $listenersOnCurrentSubscriber){
                        $listenersOnCurrentSubscriber[j]($event.currentSubscriber);
                    }
                }
            },
            getSubscribers : function(){
                return $event.subscribers;
            },
            setEvent : function(event){
                if(event){
                    $event.event = event;
                    $event.links.event = buildLinks(event.links);
                    $event.event.picto = $service.getEventLinks().pictoFinish;
                    if(event.fromDate){
                        event.fromDate = new Date(event.fromDate);
                    }
                    for(var j in $listenersOnEvent){
                        $listenersOnEvent[j]($event.event);
                    }
                }else{
                     for(var j in $listenersOnEvent){
                         $listenersOnEvent[j]();
                     }
                }
            },
            setSubscribers : function(subscribers){
                $event.subscribers = subscribers;
                for(var i in $listenersOnSubscribers){
                    $listenersOnSubscribers[i]($event.subscribers);
                }
            },
            loadEvent : function(id, user, callback){
                $event.currentUser = user;
                var idUser = user.id;
                $http.get('/rest/events/'+id).success(function(event) {
                    if(event){
                        $service.setEvent(event);
                        mapService.addMarkerEvent($event.event);
                        if($event.event.subscribers){
                            $service.initSubscribers(idUser, $event.event.subscribers);
                        }
                        if(callback){
                            callback($event);
                        }
                    }
               });
            },
            reloadSubscribers : function (){
                var subscribersLink = $service.getEventLinks().subscribers;
                $http.get(subscribersLink).success(function (subscribers){
                    if(subscribers){
                        var idUser = null;
                        if($event.currentSubscriber && $event.currentSubscriber.userRef){
                            idUser = $event.currentSubscriber.userRef;
                        }else if($event.currentUser){
                            idUser = $event.currentUser.id;
                        }
                        if(idUser){
                            $service.initSubscribers(idUser, subscribers);
                        }
                    }
                }).error(function(error){
                });
            },
            indexSubscribers : function (subscribers){
                for(var i in subscribers){
                    var subscriber = subscribers[i];
                    $event.links.subscribers[subscriber.userRef] = buildLinks(subscriber.links);
                    if(subscriber.car && subscriber.car.links){
                        $event.links.subscribers[subscriber.userRef].carlinks = buildLinks(subscriber.car.links);
                    }
                    $service.setSubscriber(subscriber);
                }
            },
            prepareSubscriber : function(subscriber){

                if(subscriber){
                    subscriber.visible=true;
                    subscriber.current = false;
                    //le user courant est un chauffeur et subscriber est à pied
                    if($event.currentSubscriber && $event.currentSubscriber.locomotion==='CAR' && subscriber.locomotion==='AUTOSTOP'){
                        if(subscriber.carRef == $event.currentSubscriber.userRef){
                            subscriber.picto = $service.getEventLinks().pictoMyPassenger
                            subscriber.inMyCar = true;
                        }else if($event.currentSubscriber.car && $event.currentSubscriber.car.waitingList && $event.currentSubscriber.car.waitingList.length>0 && inArray(subscriber.userRef, $event.currentSubscriber.car.waitingList)){
                            subscriber.picto = $service.getEventLinks().pictoStop;
                            subscriber.waitingForMyCar = true;
                        }else if(inArray($event.currentSubscriber.userRef, subscriber.possibleCars)){
                            subscriber.picto = $service.getEventLinks().pictoStop;
                            subscriber.requestedByMe = true;
                        }else{
                            subscriber.picto = $service.getEventLinks().pictoStopDark;
                            subscriber.free = true;
                        }
                    }else if($event.currentSubscriber && $event.currentSubscriber.locomotion==='AUTOSTOP' && subscriber.locomotion==='CAR'){
                        if($event.currentSubscriber.carRef === subscriber.userRef){
                            subscriber.picto = $service.getEventLinks().pictoMyCar;
                            subscriber.currentCar = true;
                        }else if(subscriber.car && subscriber.car.waitingList && subscriber.car.waitingList.length>0 &&
                                inArray($event.currentSubscriber.userRef, subscriber.car.waitingList)){
                            subscriber.picto = $service.getEventLinks().pictoCar;
                            subscriber.iAskHim = true;
                        }else if(subscriber.userRef &&
                                inArray(subscriber.userRef, $event.currentSubscriber.possibleCars)){
                            subscriber.picto = $service.getEventLinks().pictoCar;
                            subscriber.heAskMeToBeInHisCar = true;
                        }else{
                            subscriber.picto = $service.getEventLinks().pictoCarDark;
                            subscriber.normalCar = true;
                        }
                    }else if($event.currentSubscriber && subscriber.userRef == $event.currentSubscriber.userRef){
                        subscriber.current = true;
                        if($event.currentSubscriber && $event.currentSubscriber.locomotion==='CAR'){
                            subscriber.picto = $service.getEventLinks().pictoMyCar;
                        }else if($event.currentSubscriber && $event.currentSubscriber.locomotion==='AUTOSTOP'){
                            subscriber.picto = $service.getEventLinks().pictoMyPassenger;
                        }
                    }else if(subscriber.locomotion==='CAR'){
                        subscriber.picto = $service.getEventLinks().pictoCarDark;
                    }else if(subscriber.locomotion==='AUTOSTOP'){
                        subscriber.picto = $service.getEventLinks().pictoStopDark;
                    }else{
                        subscriber.picto = $service.getEventLinks().pictoDontKnow;
                        subscriber.dontKnow = true;
                    }

                    return subscriber;
                }else{
                    return null;
                }
            },
            initSubscribers : function(currentUserId, subscribers, callback){

                $service.indexSubscribers(subscribers);
                $service.setCurrentSubscriber($service.getSubscriber(currentUserId));

                for(var i in subscribers){
                    var subscriber = subscribers[i];
                    $service.prepareSubscriber(subscriber);
                    mapService.addMarkerSubscriber(subscriber);
                }

                $service.setSubscribers(subscribers)
                $service.loadCurrentCar();
                if($event.currentCar){
                    var points = new Array();
                    if($event.currentCar.passengers){
                        for(var i in $event.currentCar.passengers){
                            points.push($event.currentCar.passengers[i].subscriber.address);
                        }
                    }
                    mapService.traceDirections($event.event.address, $event.currentCar.driver.subscriber.address, points);
                }

                if(callback){
                    callback($event.subscribers);
                }

            },
            applyFilter : function(filterUsers){
                for(var i in $event.subscribers){
                    var subscriber = $event.subscribers[i];
                    if(filterUsers == "AUTOSTOP"){
                        if(subscriber.locomotion!="AUTOSTOP"){
                            subscriber.visible = false;
                        }else{
                            subscriber.visible = true;
                        }
                    }else if(filterUsers == "CAR"){
                        if(subscriber.locomotion!="CAR"){
                            subscriber.visible = false;
                        }else{
                            subscriber.visible = true;
                        }
                    }
                }
            },
            getCurrentCar : function(){
                return $event.currentCar;
            },
            setCurrentCar : function(car){
                $event.currentCar = car;
                for(var j in $listenersOnCurrentCar){
                    $listenersOnCurrentCar[j]($event.currentCar);
                }
            },
            loadCurrentCar : function(){
                if($event.currentSubscriber){
                    if($event.currentSubscriber.locomotion == 'CAR' && $event.currentSubscriber.car){
                        $service.setCurrentCar(buildCarObject($event.currentSubscriber));
                    }else if( $event.currentSubscriber.locomotion=='AUTOSTOP' && $event.currentSubscriber.carRef){
                        $service.setCurrentCar(buildCarObject($service.getSubscriber($event.currentSubscriber.carRef)));
                    }else{
                        $service.setCurrentCar();
                    }
                }
            },
            ///////// APPELS REST ////////
            securise : function(callback){
                var deferred = $q.defer();
                $http.put($service.getEventLinks().securised, {value:true}).success(function(data){
                    deferred.resolve(data);
                }).error(function(error){
                    deferred.reject(error);
                });
                return deferred.promise ;
            },
            unSecurise : function(callback){
                var deferred = $q.defer();
                $http.put($service.getEventLinks().securised, {value:false}).success(function(data){
                    deferred.resolve(data);
                }).error(function(error){
                    deferred.reject(data);
                });
                return deferred.promise ;
            },
            subscribe : function(user, callbackSuccess, callbackError){
                var deferred = $q.defer();
                if(user && user.userRef){
                    $http.post($service.getEventLinks().subscribers, user).success(function(data){
                        $service.reloadSubscribers();
                        deferred.resolve(data);
                    }).error(function(data){
                        deferred.reject(data);
                    });
                }else{
                    deferred.reject();
                }
                return deferred.promise ;
            },
            updateLocomotion : function(locomotion){
                var deferred = $q.defer();
                var link = $service.getSubscriberLinks($event.currentSubscriber.userRef).locomotion;
                $http.put(link, {locomotion : locomotion}).success(function(data){
                    $service.reloadSubscribers();
                    deferred.resolve(data);
                }).error(function(data){
                    deferred.reject(data);
                });
                return deferred.promise ;
            },
            saveSubscriber : function(subscriber, callback, callbackError){
                var deferred = $q.defer();
                if(subscriber){
                    $http.put($service.getSubscriberLinks(subscriber.userRef).self, subscriber).success(function(data){
                        mapService.updateMarkerSubscriber(subscriber);
                        $service.reloadSubscribers();
                        deferred.resolve(data);
                    }).error(function(error){
                        deferred.reject(error);
                    });
                }else{
                    deferred.reject();
                }
                return deferred.promise ;
            },

            /////// GESTION DES PLACES DANS LES VOITURES /////////////////////
            proposeSeat : function(user, callback, callbackError){
                var deferred = $q.defer();
                if($event.currentSubscriber){
                    var carRef = $event.currentSubscriber.userRef;
                    var link = $service.getSubscriberLinks(user.userRef).addPossibleCar;
                    $http.post(link, {car:carRef}).success(function(data){
                        $service.reloadSubscribers();
                        deferred.resolve(data);
                    }).error(function(data){
                        deferred.reject(data);
                    });
                }else{
                    deferred.reject();
                }
                return deferred.promise ;
            },
            removeWaitingGuy : function(car, passenger, callback, callbackError){
                var deferred = $q.defer();
                var links = $service.getSubscriberLinks(car.userRef);
                if(links && links.carlinks && links.carlinks.waitingList){
                    var link = links.carlinks.waitingList;
                    $http({
                        url: link+'/'+passenger,
                        dataType: 'json',
                        method: 'DELETE',
                        data: {},
                        headers: {
                            "Content-Type": "application/json"
                        }
                    }).success(function(data){
                        $service.reloadSubscribers();
                        deferred.resolve(data);
                    }).error(function(data){
                        deferred.reject(data);
                    });
                }else{
                    deferred.reject();
                }
                return deferred.promise ;
            },
            validatePassenger : function(car, passenger, callback, callbackError){
                var deferred = $q.defer();
                var link = $service.getSubscriberLinks(car.userRef).car;
                $http.post(link, {passenger:passenger}).success(function(data){
                    $service.reloadSubscribers();
                    deferred.resolve(data);
                }).error(function(data){
                    deferred.reject(data);
                });
                return deferred.promise ;
            },
            removePassenger : function(car, passenger, callback, callbackError){
                var deferred = $q.defer();
                var link = $service.getSubscriberLinks(car.userRef).car;
                $http({
                    url: link+'/'+passenger,
                    dataType: 'json',
                    method: 'DELETE',
                    data: {},
                    headers: {
                        "Content-Type": "application/json"
                    }
                }).success(function(data){
                    $service.reloadSubscribers();
                    deferred.resolve(data);
                }).error(function(data){
                    deferred.reject(data);
                });
                return deferred.promise ;
            },
            //AUTOSTOPER
            askForSeat : function(car, callback, callbackError){
                var deferred = $q.defer();
                if($event.currentSubscriber){
                    var passenger = $event.currentSubscriber.userRef;
                    var links = $service.getSubscriberLinks(car.userRef);
                    if(links && links.carlinks && links.carlinks.waitingList){
                        var link = links.carlinks.waitingList;
                        $http.post(link, {passenger:passenger}).success(function(data){
                            $service.reloadSubscribers();
                            deferred.resolve(data);
                        }).error(function(error){
                            deferred.reject(data);
                        });
                    }else{
                        deferred.reject();
                    }
                }else{
                    deferred.reject();
                }
                return deferred.promise ;
            },
            removePossibleCar : function(passenger, carRef, callback, callbackError){
                var deferred = $q.defer();
                var link = $service.getSubscriberLinks(passenger.userRef).addPossibleCar;
                $http({
                    url:link+'/'+carRef,
                    dataType: 'json',
                    method: 'DELETE',
                    data: {},
                    headers: {
                        "Content-Type": "application/json"
                    }
                }).success(function(data){
                    $service.reloadSubscribers();
                    deferred.resolve(data);
                }).error(function(data){
                    deferred.reject(data);
                });
                return deferred.promise ;
            },

            formatErrors : function(data, callback){
                var result = new Array();
                if(data && data.errors){
                    var obj = JSON.parse(JSON.stringify(data.errors));
                    var someErrors = data.errors;
                    for(var anError in someErrors){
                        var msgs = someErrors[anError];
                        if(typeof msgs === 'string'){
                            callback(msgs);
                        }else{
                           for(var i in msgs){
                               if(callback){
                                   callback(msgs[i]);
                               }
                           }
                        }
                    }
                }else{
                    if(callback){
                        callback(data);
                    }
                }
            }
        };

	    return $service;
   }]);
});
   