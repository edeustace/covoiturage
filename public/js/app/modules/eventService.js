
angular.module('eventService', ['mapService'], function($provide){

   $provide.factory('eventService', function(mapService, $http) {

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


        var data = {};


        var $service = {
            saveSubscriber : function(subscriber, $scope){
                mapService.updateMarkerSubscriber(subscriber, $scope);
            	$service.reloadSubscribers($scope);
            },
            buildLinks :function (links){
                var theLinks = {};
                if(links){
                    for ( var i = 0; i < links.length; i++) {
                        var link = links[i];
                        theLinks[link.rel] = link.href;
                    }
                }
                return theLinks;
            },
            setCurrentWidowsSubscriber : function($scope){
                if($scope.currentInfoWindowsSubscriber){
                    var marker = mapService.findMarkerByUserRef($scope.currentInfoWindowsSubscriber.userRef);
                    var subscriber = $scope.refSubscribers[$scope.currentInfoWindowsSubscriber.userRef];
                    if(marker && subscriber){
                        $scope.currentMarker = marker;
                        $scope.currentInfoWindowsSubscriber = subscriber;
                    }
                }
            } ,
            reloadSubscribers : function ($scope){
                var subscribersLink = $scope.eventLinks.subscribers;
                $http.get(subscribersLink).success(function (subscribers){
                    if(subscribers){
                        $service.initSubscribers($scope, subscribers, function(subscriber){
                        });
                        $scope.event.subscribers = subscribers;
                        $service.setCurrentWidowsSubscriber($scope);
                    }
                }).error(function(error){
                    alert(error);
                });
            },
            updateSubscriber : function ($scope, subscriber, callback){
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
            },
            initSubscribers : function($scope, subscribers, callback){
                var length = subscribers.length;
                var idUser = $scope.user.id;
                $scope.refSubscribers = {};
                for(var i=0; i<length; i++){
                    var subscriber = subscribers[i];
                    $service.updateSubscriber($scope, subscriber, callback)
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

                    mapService.addMarkerSubscriber(subscriber, $scope);
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
                mapService.traceDirections($scope);
            }
        };

	    return $service;
   });
});
   