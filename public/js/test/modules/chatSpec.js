'use strict';

var jsRoutes;

describe('chatSpec', function() {

    var service, $httpBackend;

    beforeEach(module('chatModule'));
    beforeEach(function(){
        module(function($provide) {

        });
        jsRoutes = {
            controllers : {
                ChatCtrl : {
                    getTopics : function(eventId, categorie, currentUserId){
                        var url = '/rest/topics/'+eventId;

                        if(categorie){
                            url = url+'?categorie='+categorie;
                        }

                        return {url:url};
                    },
                    updateTopic : function(eventId, idTopic){
                       return {url:'/rest/events/'+eventId+'/topics/'+idTopic};
                    },
                    getMessages : function(id){
                        return {url:'/rest/messages/'+id};
                    },
                    createTopic : function(id){
                        return {url:'/rest/events/'+id+'/topics'};
                    }
                },
            },
        };


        inject(function($injector) {
            $httpBackend = $injector.get('$httpBackend');
            $httpBackend.when('GET', '/rest/topics/1').respond(
                [
                    {
                        id : '1',
                        date : 1374692153918,
                        update : 1374692153918,
                    },
                    {
                        id : '2',
                        date : 1374692153917,
                        update : 1374692153917,
                    }
                ]
            );
            $httpBackend.when('GET', '/rest/topics/1?categorie=wall').respond(
                [
                    {
                        id : '1',
                        date : 1374692153918,
                        update : 1374692153918,
                    },
                ]
            );
            $httpBackend.when('GET', '/rest/messages/1').respond(
                [
                    {
                        id : '1',
                        date : 1374692153917,
                        message : 'test'
                    }
                ]
            );
            $httpBackend.when('POST', '/rest/events/123456/topics').respond({
                id:'1'
            });
            $httpBackend.when('PUT', '/rest/events/123456/topics/1').respond({
                id:'1'
            });

            service = $injector.get('chatService');
        });
    });

    describe('loadTopics', function() {

        it('cas passant', function() {
            var data = {};
            $httpBackend.expectGET('/rest/topics/1');
            $httpBackend.expectGET('/rest/messages/1');
            service.init(data);
            service.loadTopics('1', '2');
            $httpBackend.flush();

            expect(data.chat.topics).not.toBe(undefined);
            expect(data.chat.topics).not.toBe(null);
            expect(data.chat.topics.length).toEqual(2);
            expect(data.chat.topics[0].id).toEqual('1');
            expect(data.chat.topics[0].active).toBe(true);
            expect(data.chat.topics[0].date).toEqual(new Date(1374692153918));
            expect(data.chat.topics[0].update).toEqual(new Date(1374692153918));
            expect(data.chat.topics[1].id).toEqual('2');
            expect(data.chat.topics[1].active).toBe(false);
            expect(data.chat.topics[1].date).toEqual(new Date(1374692153917));
            expect(data.chat.topics[1].update).toEqual(new Date(1374692153917));
            expect(data.chat.currentTopic.id).toEqual('1');
            expect(data.chat.messages.length).toEqual(1);
            expect(data.chat.messages[0].id).toEqual('1');
            expect(data.chat.messages[0].date).toEqual(new Date(1374692153917));
            expect(data.chat.messages[0].message).toEqual('test');
        });

    });
    describe('loadWall', function() {

        it('cas passant', function() {
            var data = {};
            $httpBackend.expectGET('/rest/topics/1?categorie=wall');
            $httpBackend.expectGET('/rest/messages/1');
            service.init(data);
            service.loadWall('1');
            $httpBackend.flush();

            expect(data.wall.topic).not.toBe(undefined);
            expect(data.wall.topic).not.toBe(null);
            expect(data.wall.topic.id).toEqual('1');
            expect(data.wall.topic.date).toEqual(new Date(1374692153918));
            expect(data.wall.topic.update).toEqual(new Date(1374692153918));
            expect(data.wall.messages.length).toEqual(1);
            expect(data.wall.messages[0].id).toEqual('1');
            expect(data.wall.messages[0].date).toEqual(new Date(1374692153917));
            expect(data.wall.messages[0].message).toEqual('test');
        });

    });
    describe('loadMessages', function() {

        it('cas passant', function() {
            $httpBackend.expectGET('/rest/messages/1');
            var data = {};
            service.init(data);
            service.pushTopic({id:'1'});
            service.loadMessages({id:'1'});
            $httpBackend.flush();
            expect(data.chat.currentTopic.id).toEqual('1');
            expect(data.chat.messages.length).toEqual(1);
            expect(data.chat.messages[0].id).toEqual('1');
            expect(data.chat.messages[0].date).toEqual(new Date(1374692153917));
            expect(data.chat.messages[0].message).toEqual('test');

        });

        it('rien', function() {
            $httpBackend.expectGET('/rest/messages/1');
            var data = {};
            service.init(data);
            service.loadMessages();
            expect(data.chat.currentTopic).toBe(null);
            expect(data.chat.messages.length).toEqual(0);
        });

    });

    describe('addTopic', function() {

        it('cas passant', function() {
            var data = {};
            service.init(data);
            data.chat.topics.push({
                id : '1',
                subscribers :[]
            });

            service.addTopic({
                statut : 'CREATED',
                data :{
                    id : '2',
                    subscribers:['1']
                }
            }, '1');
            expect(data.chat.topics).not.toBe(undefined);
            expect(data.chat.topics).not.toBe(null);
            expect(data.chat.topics.length).toEqual(2);
        });

        it('Topic existant 1', function() {
            var data = {};
            service.init(data);
            data.chat.topics.push({
                id : '1',
                subscribers :[]
            });

            service.addTopic({
                 statut : 'UPDATED',
                 data :{
                     id : '1',
                     subscribers : ['1','2']
                 }
             }, '1');
            expect(data.chat.topics).not.toBe(undefined);
            expect(data.chat.topics).not.toBe(null);
            expect(data.chat.topics.length).toEqual(1);
            expect(data.chat.topics[0].subscribers.length).toEqual(2);
        });

        it('Topic existant 2', function() {
            var data = {};
            service.init(data);
            data.chat.topics.push({
                id : '1',
                tmpId : '3',
                subscribers :['toto', 'tata']
            });

            service.addTopic({
                tmpId : '3',
                subscribers :['toto', 'tata']
            }, '1');
            expect(data.chat.topics).not.toBe(undefined);
            expect(data.chat.topics).not.toBe(null);
            expect(data.chat.topics.length).toEqual(1);
        });
        it('Topic a supprimer ', function() {
            var data = {};
            service.init(data);
            data.chat.topics.push({
                id : '1',
                subscribers :['1', '2']
            });

            service.addTopic({
                statut : 'UPDATED',
                 data :{
                     id : '1',
                     subscribers : ['2','3']
                 }
             }, '1');
            expect(data.chat.topics).not.toBe(undefined);
            expect(data.chat.topics).not.toBe(null);
            expect(data.chat.topics.length).toEqual(0);
        });

        it('Topic a supprimer 2', function() {
            var data = {};
            service.init(data);
            data.chat.topics.push({
                id : '1',
                subscribers :['1', '2']
            });

            service.addTopic({
                statut : 'DELETED',
                 data :{
                     id : '1',
                     subscribers : ['1','2']
                 }
             }, '1');
            expect(data.chat.topics).not.toBe(undefined);
            expect(data.chat.topics).not.toBe(null);
            expect(data.chat.topics.length).toEqual(0);
        });

    });

    describe('createTopic', function() {

        it('Déjà existant', function() {

            $httpBackend.expectGET('/rest/messages/1');
            var data = {};
            service.init(data);
            data.chat.topics.push({
                id : '1',
                tmpId : '3',
                subscribers :['1', '2']
            });
            service.createTopic('123456', {userRef:'1'}, {userRef:'2'});
            $httpBackend.flush();
            expect(data.chat.topics).not.toBe(undefined);
            expect(data.chat.topics).not.toBe(null);
            expect(data.chat.topics.length).toEqual(1);

            expect(data.chat.messages[0].id).toEqual('1');
            expect(data.chat.messages[0].date).toEqual(new Date(1374692153917));
            expect(data.chat.messages[0].message).toEqual('test');

        });
        it('Nouveau', function() {

            var data = {};
            service.init(data);
            data.chat.topics.push({
                id : '2',
                tmpId : '3',
                subscribers :['3', '2']
            });
            service.createTopic('123456', {userRef:'1'}, {userRef:'2'});
            $httpBackend.flush();
            expect(data.chat.topics).not.toBe(undefined);
            expect(data.chat.topics).not.toBe(null);
            expect(data.chat.topics.length).toEqual(2);
            expect(data.chat.messages[0].id).toEqual('1');
            expect(data.chat.messages[0].date).toEqual(new Date(1374692153917));
            expect(data.chat.messages[0].message).toEqual('test');

        });
    });

    describe('createTopic for car', function() {

        it('Déjà existant', function() {

            $httpBackend.expectGET('/rest/messages/1');
            var data = {};
            service.init(data);
            data.chat.topics.push({
                id : '1',
                tmpId : '3',
                categorie:'carChat',
                subscribers :['1', '2', '3']
            });
            service.createTopicForCar('123456', {driver:{id:'1'}, passengers:[{id:'2'},{id:'3'}]});
            $httpBackend.flush();
            expect(data.chat.topics).not.toBe(undefined);
            expect(data.chat.topics).not.toBe(null);
            expect(data.chat.topics.length).toEqual(1);

            expect(data.chat.messages[0].id).toEqual('1');
            expect(data.chat.messages[0].date).toEqual(new Date(1374692153917));
            expect(data.chat.messages[0].message).toEqual('test');

        });
        it('Déjà existant avec maj', function() {

            $httpBackend.expectGET('/rest/messages/1');
            var data = {};
            service.init(data);
            data.chat.topics.push({
                id : '1',
                tmpId : '3',
                categorie:'carChat',
                subscribers :['1', '2']
            });
            service.createTopicForCar('123456', {driver:{id:'1'}, passengers:[{id:'2'},{id:'3'}]});
            $httpBackend.flush();
            expect(data.chat.topics).not.toBe(undefined);
            expect(data.chat.topics).not.toBe(null);
            expect(data.chat.topics.length).toEqual(1);
            expect(data.chat.topics[0].id).toEqual('1');
            expect(data.chat.topics[0].subscribers.length).toEqual(3);
            expect(data.chat.topics[0].subscribers[0]).toEqual('1');
            expect(data.chat.topics[0].subscribers[1]).toEqual('2');
            expect(data.chat.topics[0].subscribers[2]).toEqual('3');

            expect(data.chat.messages[0].id).toEqual('1');
            expect(data.chat.messages[0].date).toEqual(new Date(1374692153917));
            expect(data.chat.messages[0].message).toEqual('test');
        });
        it('Nouveau', function() {

            var data = {};
            service.init(data);
            data.chat.topics.push({
                id : '2',
                tmpId : '3',
                subscribers :['3', '2']
            });
            service.createTopicForCar('123456', {driver:{id:'1'}, passengers:[{id:'2'},{id:'3'}]});
            $httpBackend.flush();
            expect(data.chat.topics).not.toBe(undefined);
            expect(data.chat.topics).not.toBe(null);
            expect(data.chat.topics.length).toEqual(2);
            expect(data.chat.topics[1].categorie).toEqual('carChat');
            expect(data.chat.topics[1].subscribers.length).toEqual(3);
            expect(data.chat.topics[1].subscribers[0]).toEqual('1');
            expect(data.chat.topics[1].subscribers[1]).toEqual('2');
            expect(data.chat.topics[1].subscribers[2]).toEqual('3');
            expect(data.chat.messages[0].id).toEqual('1');
            expect(data.chat.messages[0].date).toEqual(new Date(1374692153917));
            expect(data.chat.messages[0].message).toEqual('test');
        });
    });
});