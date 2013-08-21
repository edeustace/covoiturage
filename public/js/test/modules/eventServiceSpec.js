'use strict';

/* jasmine specs for services go here */

function clone(obj){
    return JSON.parse(JSON.stringify(obj));
}

describe('eventServiceSpec', function() {

    var eventMock = {subscribers:[]};
    var $httpBackend, $rootScope, service;

    beforeEach(module('eventModule'));
    beforeEach(function(){
        module(function($provide) {
            $provide.value('mapService',
                {
                    addMarkerSubscriber:function(){},
                    traceDirections:function(){},
                    addMarkerEvent:function(){}
                });
        });

        inject(function($injector) {
            $httpBackend = $injector.get('$httpBackend');
            $httpBackend.when('GET', '/rest/events/51cb46c544aec7e66152169d').respond(clone(eventMockFull));

            service = $injector.get('eventService');
        });
    });

    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    describe('setEvent', function() {
        it('setEvent', function() {
            var currentEvent = clone(eventMockFull);
            service.addListenerOnEvent(function(event){
                expect(event.id).toEqual(currentEvent.id);
                expect(event.picto).toEqual("/assets/icons/finish.png");
            });
            service.setEvent(currentEvent);
        });

        it('setEvent null', function() {

            service.addListenerOnEvent(function(event){
                expect(event).toEqual(undefined);
            });
            service.setEvent();
        });
    });

    describe('prepareSubscriber', function() {
        it('undefined', function() {
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            var subscriber = service.prepareSubscriber(null);
            expect(subscriber).toBeNull();
        });


        it('There s no current subscriber and car ', function() {
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            var subscriber = service.prepareSubscriber({userRef:'123465',locomotion:'CAR'});
            expect(subscriber.visible).toBe(true);
            expect(subscriber.current).toBe(false);

            expect(subscriber.inMyCar).toBe(undefined);
            expect(subscriber.waitingForMyCar).toBe(undefined);
            expect(subscriber.requestedByMe).toBe(undefined);
            expect(subscriber.free).toBe(undefined);

            expect(subscriber.currentCar).toBe(undefined);
            expect(subscriber.iAskHim).toBe(undefined);
            expect(subscriber.heAskMeToBeInHisCar).toBe(undefined);
            expect(subscriber.normalCar).toBe(undefined);

            expect(subscriber.picto).toEqual('/assets/icons/car_dark.png');
        });

        it('There s no current subscriber and autostop ', function() {
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            var subscriber = service.prepareSubscriber({userRef:'123465',locomotion:'AUTOSTOP'});
            expect(subscriber.visible).toBe(true);
            expect(subscriber.current).toBe(false);

            expect(subscriber.inMyCar).toBe(undefined);
            expect(subscriber.waitingForMyCar).toBe(undefined);
            expect(subscriber.requestedByMe).toBe(undefined);
            expect(subscriber.free).toBe(undefined);

            expect(subscriber.currentCar).toBe(undefined);
            expect(subscriber.iAskHim).toBe(undefined);
            expect(subscriber.heAskMeToBeInHisCar).toBe(undefined);
            expect(subscriber.normalCar).toBe(undefined);
            expect(subscriber.picto).toEqual('/assets/icons/pedestriancrossing_green-dark.png');
        });

        it('Current subscriber', function() {
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            service.indexSubscribers(currentEvent.subscribers);
            var current = service.getSubscriber("51c9f4d744aeec534e2a86d3");
            service.setCurrentSubscriber(current);
            var subscriber = service.prepareSubscriber(current);
            expect(subscriber.visible).toBe(true);
            expect(subscriber.current).toBe(true);

            expect(subscriber.inMyCar).toBe(undefined);
            expect(subscriber.waitingForMyCar).toBe(undefined);
            expect(subscriber.requestedByMe).toBe(undefined);
            expect(subscriber.free).toBe(undefined);

            expect(subscriber.currentCar).toBe(undefined);
            expect(subscriber.iAskHim).toBe(undefined);
            expect(subscriber.heAskMeToBeInHisCar).toBe(undefined);
            expect(subscriber.normalCar).toBe(undefined);

            expect(subscriber.picto).toEqual('/assets/icons/pedestriancrossing_red.png');
        });

        it('Dont know yet and current subscriber autostop', function() {
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            service.indexSubscribers(currentEvent.subscribers);
            service.setCurrentSubscriber(service.getSubscriber("51c9f4d744aeec534e2a86d3"));
            var subscriber = service.prepareSubscriber(service.getSubscriber("51cff92644ae0cf0eabc5546"));
            expect(subscriber.visible).toBe(true);
            expect(subscriber.current).toBe(false);

            expect(subscriber.inMyCar).toBe(undefined);
            expect(subscriber.waitingForMyCar).toBe(undefined);
            expect(subscriber.requestedByMe).toBe(undefined);
            expect(subscriber.free).toBe(undefined);

            expect(subscriber.currentCar).toBe(undefined);
            expect(subscriber.iAskHim).toBe(undefined);
            expect(subscriber.heAskMeToBeInHisCar).toBe(undefined);
            expect(subscriber.normalCar).toBe(undefined);

            expect(subscriber.picto).toEqual('/assets/icons/symbol_blank.png');
        });


        it('Car owner and current subscriber autostop', function() {
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            service.indexSubscribers(currentEvent.subscribers);
            service.setCurrentSubscriber(service.getSubscriber("51c9f4d744aeec534e2a86d3"));
            var subscriber = service.prepareSubscriber(service.getSubscriber("51c9f43c44aeec534e2a868f"));
            expect(subscriber.visible).toBe(true);
            expect(subscriber.current).toBe(false);

            expect(subscriber.inMyCar).toBe(undefined);
            expect(subscriber.waitingForMyCar).toBe(undefined);
            expect(subscriber.requestedByMe).toBe(undefined);
            expect(subscriber.free).toBe(undefined);

            expect(subscriber.currentCar).toBe(undefined);
            expect(subscriber.iAskHim).toBe(undefined);
            expect(subscriber.heAskMeToBeInHisCar).toBe(undefined);
            expect(subscriber.normalCar).toBe(true);

            expect(subscriber.picto).toEqual('/assets/icons/car_dark.png');
        });

        it('Autostop and current subscriber car', function() {
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            service.indexSubscribers(currentEvent.subscribers);
            service.setCurrentSubscriber(service.getSubscriber("51c9f43c44aeec534e2a868f"));
            var subscriber = service.prepareSubscriber(service.getSubscriber("51c9f4d744aeec534e2a86d3"));
            expect(subscriber.visible).toBe(true);
            expect(subscriber.current).toBe(false);

            expect(subscriber.inMyCar).toBe(undefined);
            expect(subscriber.waitingForMyCar).toBe(undefined);
            expect(subscriber.requestedByMe).toBe(undefined);
            expect(subscriber.free).toBe(true);

            expect(subscriber.currentCar).toBe(undefined);
            expect(subscriber.iAskHim).toBe(undefined);
            expect(subscriber.heAskMeToBeInHisCar).toBe(undefined);
            expect(subscriber.normalCar).toBe(undefined);

            expect(subscriber.picto).toEqual('/assets/icons/pedestriancrossing_green-dark.png');
        });

        it('In my car', function() {
            var userInCar = "51cb52ec44aec7e661521c4d";
            var carOwner = "51cdb98744ae8fb5cb9d970b";
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            service.indexSubscribers(currentEvent.subscribers);
            service.setCurrentSubscriber(service.getSubscriber(carOwner));
            var subscriber = service.prepareSubscriber(service.getSubscriber(userInCar));
            expect(subscriber.visible).toBe(true);
            expect(subscriber.current).toBe(false);

            expect(subscriber.inMyCar).toBe(true);
            expect(subscriber.waitingForMyCar).toBe(undefined);
            expect(subscriber.requestedByMe).toBe(undefined);
            expect(subscriber.free).toBe(undefined);

            expect(subscriber.currentCar).toBe(undefined);
            expect(subscriber.iAskHim).toBe(undefined);
            expect(subscriber.heAskMeToBeInHisCar).toBe(undefined);
            expect(subscriber.normalCar).toBe(undefined);

            expect(subscriber.picto).toEqual('/assets/icons/pedestriancrossing_red.png');
        });

        it('My current car', function() {
            var userInCar = "51cdb98744ae8fb5cb9d970b";
            var carOwner = "51cb52ec44aec7e661521c4d";
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            service.indexSubscribers(currentEvent.subscribers);
            service.setCurrentSubscriber(service.getSubscriber(carOwner));
            var subscriber = service.prepareSubscriber(service.getSubscriber(userInCar));
            expect(subscriber.visible).toBe(true);
            expect(subscriber.current).toBe(false);

            expect(subscriber.inMyCar).toBe(undefined);
            expect(subscriber.waitingForMyCar).toBe(undefined);
            expect(subscriber.requestedByMe).toBe(undefined);
            expect(subscriber.free).toBe(undefined);

            expect(subscriber.currentCar).toBe(true);
            expect(subscriber.iAskHim).toBe(undefined);
            expect(subscriber.heAskMeToBeInHisCar).toBe(undefined);
            expect(subscriber.normalCar).toBe(undefined);

            expect(subscriber.picto).toEqual('/assets/icons/car_red.png');
        });

        it('Waiting for my car', function() {
            var userWaiting = "51c34e2444ae97756acc3bcf";
            var carOwner = "51cdb98744ae8fb5cb9d970b";
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            service.indexSubscribers(currentEvent.subscribers);
            service.setCurrentSubscriber(service.getSubscriber(carOwner));
            var subscriber = service.prepareSubscriber(service.getSubscriber(userWaiting));
            expect(subscriber.visible).toBe(true);
            expect(subscriber.current).toBe(false);

            expect(subscriber.inMyCar).toBe(undefined);
            expect(subscriber.waitingForMyCar).toBe(true);
            expect(subscriber.requestedByMe).toBe(undefined);
            expect(subscriber.free).toBe(undefined);

            expect(subscriber.currentCar).toBe(undefined);
            expect(subscriber.iAskHim).toBe(undefined);
            expect(subscriber.heAskMeToBeInHisCar).toBe(undefined);
            expect(subscriber.normalCar).toBe(undefined);

            expect(subscriber.picto).toEqual('/assets/icons/pedestriancrossing_green-classic.png');
        });

        it('I ask him to be in his car', function() {
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            service.indexSubscribers(currentEvent.subscribers);
            var userWaiting = service.getSubscriber("51c34e2444ae97756acc3bcf");
            var carOwner = service.getSubscriber("51cdb98744ae8fb5cb9d970b");
            service.setCurrentSubscriber(userWaiting);
            var aSubscriber = service.prepareSubscriber(carOwner);
            expect(aSubscriber.visible).toBe(true);
            expect(aSubscriber.current).toBe(false);

            expect(aSubscriber.inMyCar).toBe(undefined);
            expect(aSubscriber.waitingForMyCar).toBe(undefined);
            expect(aSubscriber.requestedByMe).toBe(undefined);
            expect(aSubscriber.free).toBe(undefined);

            expect(aSubscriber.currentCar).toBe(undefined);
            expect(aSubscriber.iAskHim).toBe(true);
            expect(aSubscriber.heAskMeToBeInHisCar).toBe(undefined);
            expect(aSubscriber.normalCar).toBe(undefined);

            expect(aSubscriber.picto).toEqual('/assets/icons/car_classic.png');
        });

        it('He ask me to be in his car', function() {
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            service.indexSubscribers(currentEvent.subscribers);
            var userWaiting = service.getSubscriber("5200d67544ae8c9c8dbb467e");
            var carOwner = service.getSubscriber("52015d6a44ae8c9c8dbb5676");
            service.setCurrentSubscriber(userWaiting);
            var aSubscriber = service.prepareSubscriber(carOwner);
            expect(aSubscriber.visible).toBe(true);
            expect(aSubscriber.current).toBe(false);

            expect(aSubscriber.inMyCar).toBe(undefined);
            expect(aSubscriber.waitingForMyCar).toBe(undefined);
            expect(aSubscriber.requestedByMe).toBe(undefined);
            expect(aSubscriber.free).toBe(undefined);

            expect(aSubscriber.currentCar).toBe(undefined);
            expect(aSubscriber.iAskHim).toBe(undefined);
            expect(aSubscriber.heAskMeToBeInHisCar).toBe(true);
            expect(aSubscriber.normalCar).toBe(undefined);

            expect(aSubscriber.picto).toEqual('/assets/icons/car_classic.png');
        });

         it('I propose my car', function() {
             var currentEvent = clone(eventMockFull);
             service.setEvent(currentEvent);
             service.indexSubscribers(currentEvent.subscribers);
             var userWaiting = service.getSubscriber("5200d67544ae8c9c8dbb467e");
             var carOwner = service.getSubscriber("52015d6a44ae8c9c8dbb5676");
             service.setCurrentSubscriber(carOwner);
             var aSubscriber = service.prepareSubscriber(userWaiting);
             expect(aSubscriber.visible).toBe(true);
             expect(aSubscriber.current).toBe(false);

             expect(aSubscriber.inMyCar).toBe(undefined);
             expect(aSubscriber.waitingForMyCar).toBe(undefined);
             expect(aSubscriber.requestedByMe).toBe(true);
             expect(aSubscriber.free).toBe(undefined);

             expect(aSubscriber.currentCar).toBe(undefined);
             expect(aSubscriber.iAskHim).toBe(undefined);
             expect(aSubscriber.heAskMeToBeInHisCar).toBe(undefined);
             expect(aSubscriber.normalCar).toBe(undefined);

             expect(aSubscriber.picto).toEqual('/assets/icons/pedestriancrossing_green-classic.png');
         });
    });

    describe('indexSubscribers', function() {
        it('0 results', function() {
            var subscribers = [
                {
                    userRef : '1',
                    links : [
                        {rel : 'self', href:'/self'},
                        {rel : 'other', href:'/other'}
                    ]
                },
                {
                    userRef : '2',
                    links : [
                        {rel : 'self', href:'/self'},
                        {rel : 'other', href:'/other'}
                    ],
                    car : {
                        links : [
                            {rel : 'self', href:'/car/self'},
                            {rel : 'other', href:'/car/other'}
                        ]
                    }
                }
            ];
            service.indexSubscribers(subscribers);
            expect(service.getSubscriber('1').userRef).toEqual('1');
            expect(service.getSubscriberLinks('1').self).toEqual('/self');
            expect(service.getSubscriberLinks('1').other).toEqual('/other');
            expect(service.getSubscriberLinks('1').carLinks).toBe(undefined);

            expect(service.getSubscriber('2').userRef).toEqual('2');
            expect(service.getSubscriberLinks('2')).not.toBe(undefined);
            expect(service.getSubscriberLinks('2').self).toEqual('/self');
            expect(service.getSubscriberLinks('2').other).toEqual('/other');
            expect(service.getSubscriberLinks('2').carlinks).not.toBe(undefined);
            expect(service.getSubscriberLinks('2').carlinks.self).toEqual('/car/self');
        });
    });

    describe('initSubscribers', function() {
        it('0 results', function() {
            var id = "51cb46c544aec7e66152169d";
            service.initSubscribers(id, eventMock.subscribers, function(result){
                expect(result.length).toEqual(0);
            });
        });

        it('12 results', function() {
            var id = "51c9f4d744aeec534e2a86d3";
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            service.initSubscribers(id, currentEvent.subscribers, function(result){
                expect(result.length).toEqual(12);
                var currentSubscriber = service.getCurrentSubscriber();
                expect(currentSubscriber.userRef).toEqual(id);
            });
        });
    });

    describe('listeners !!!', function() {
        it('subscribers', function() {
            var test = false;
            service.addListenerOnSubscribers(function(subscribers){
                test = true;
                expect(subscribers.length).toEqual(2);
                expect(subscribers[0].userRef).toEqual('1');
                expect(subscribers[1].userRef).toEqual('2');
            });
            service.setSubscribers([{userRef:'1'},{userRef:'2'}])
            expect(test).toBe(true);
        });

        it('current subscriber', function() {
            var test = false;
            service.addListenerOnCurrentSubscriber(function(subscriber){
                test = true;
                expect(subscriber.userRef).toEqual('1');
                expect(subscriber.visible).toBe(true);
                expect(subscriber.current).toBe(true);
            });
            service.setCurrentSubscriber({userRef:'1'})
            expect(test).toBe(true);
        });

        it('event', function() {
            var test = false;
            service.addListenerOnEvent(function(event){
                test = true;
                expect(event.id).toEqual('1');
            });
            service.setEvent({id:'1'})
            expect(test).toBe(true);
        });
        it('event', function() {
            var test = false;
            service.addListenerOnEvent(function(event){
                test = true;
                expect(event).toBe(undefined);
            });
            service.setEvent()
            expect(test).toBe(true);
        });
        it('current car', function() {
            var test = false;
            service.addListenerOnCurrentCar(function(car){
                test = true;
                expect(car.id).toEqual('1');
            });
            service.setCurrentCar({id:'1'})
            expect(test).toBe(true);
        });
    });

    describe('getters ', function() {
        it('current subscriber', function() {
            expect(service.getCurrentSubscriber()).toEqual({});
        });
        it('subscriber', function() {
            expect(service.getSubscriber()).toEqual({});
        });
        it('subscriber2', function() {
            expect(service.getSubscriber('1')).toEqual({});
        });
        it('subscriber3', function() {
            service.indexSubscribers([
                 {
                     userRef : '1',
                     links : [
                         {rel : 'self', href:'/self'},
                         {rel : 'other', href:'/other'}
                     ]
                 }
             ]);
            expect(service.getSubscriber('1').userRef).toEqual('1');
        });
        it('subscriber links', function() {
            expect(service.getSubscriberLinks()).toEqual({});
            expect(service.getSubscriberLinks('1')).toEqual({});
        });
        it('subscriber links', function() {
            service.indexSubscribers([
                 {
                     userRef : '1',
                     links : [
                         {rel : 'self', href:'/self'},
                         {rel : 'other', href:'/other'}
                     ]
                 }
             ]);
            expect(service.getSubscriberLinks('1').self).toEqual('/self');
        });
        it('event links', function() {
            expect(service.getEventLinks()).toEqual({});
        });
        it('event links', function() {
            service.setEvent(
                 {
                     id : '1',
                     links : [
                         {rel : 'self', href:'/self'},
                         {rel : 'other', href:'/other'}
                     ]
                 }
            );
            expect(service.getEventLinks().self).toEqual('/self');
        });
    });

    describe('load car ', function() {
        it('no car', function() {
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            service.indexSubscribers(currentEvent.subscribers);
            service.loadCurrentCar();
            expect(service.getCurrentCar()).toBe(null);
        });

        it('current user is driver', function() {
            var carOwner = "51cdb98744ae8fb5cb9d970b";
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            service.indexSubscribers(currentEvent.subscribers);
            service.setCurrentSubscriber(service.getSubscriber(carOwner));

            service.loadCurrentCar();
            expect(service.getCurrentCar()).not.toBe(undefined);
            expect(service.getCurrentCar().driver).not.toBe(undefined);
            expect(service.getCurrentCar().driver.id).toEqual(carOwner);
            expect(service.getCurrentCar().driver.subscriber.userRef).toEqual(carOwner);
            expect(service.getCurrentCar().passengers.length).toEqual(1);
            expect(service.getCurrentCar().passengers[0].id).toEqual("51cb52ec44aec7e661521c4d");
            expect(service.getCurrentCar().passengers[0].subscriber.userRef).toEqual("51cb52ec44aec7e661521c4d");
        });

        it('current user is passenger', function() {
            var currentUser = "51cb52ec44aec7e661521c4d";
            var currentEvent = clone(eventMockFull);
            service.setEvent(currentEvent);
            service.indexSubscribers(currentEvent.subscribers);
            service.setCurrentSubscriber(service.getSubscriber(currentUser));

            service.loadCurrentCar();
            expect(service.getCurrentCar()).not.toBe(undefined);
            expect(service.getCurrentCar().driver).not.toBe(undefined);
            expect(service.getCurrentCar().driver.id).toEqual("51cdb98744ae8fb5cb9d970b");
            expect(service.getCurrentCar().driver.subscriber.userRef).toEqual("51cdb98744ae8fb5cb9d970b");
            expect(service.getCurrentCar().passengers.length).toEqual(1);
            expect(service.getCurrentCar().passengers[0].id).toEqual(currentUser);
            expect(service.getCurrentCar().passengers[0].subscriber.userRef).toEqual(currentUser);
        });
    });

    describe('load event ', function() {
        it('load !', function() {
            var ok = false;
            $httpBackend.expectGET('/rest/events/51cb46c544aec7e66152169d');
            service.loadEvent("51cb46c544aec7e66152169d", "51c9f4d744aeec534e2a86d3", function(data){
                ok = true;
            });
            $httpBackend.flush();

            expect(service.getSubscribers().length).toEqual(12);

            expect(ok).toBe(true);
        });
    });

});