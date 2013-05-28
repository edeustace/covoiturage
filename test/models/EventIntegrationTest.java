package models;


import com.mongodb.DB;
import com.mongodb.Mongo;
import models.enums.Locomotion;
import net.vz.mongodb.jackson.JacksonDBCollection;
import org.junit.*;

import java.net.UnknownHostException;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class EventIntegrationTest {


    private DB currentDataBase = null;

    @Before
    public void setUp() throws UnknownHostException {
        Mongo mongoClient = new Mongo("localhost", 27017);
        currentDataBase = mongoClient.getDB("covoiturage-test");
    }

    @After
    public void tearDown(){
          currentDataBase.dropDatabase();
    }

    @Test
    public void simpleInsert(){
        running(fakeApplication(), new Runnable() {
            public void run() {
                Event.collection = JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Event.class, String.class);
                User.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("users"), User.class, String.class));

                Event event = Event.event().name("a name").save();
                assertThat(event.id()).isNotNull();

                Event evtFromDb = Event.read(event.id());
                assertThat(evtFromDb).isNotNull();
                assertThat(evtFromDb.name()).isEqualTo(event.name());
                assertThat(evtFromDb.description()).isNull();
                assertThat(evtFromDb.creator().empty()).isTrue();
                assertThat(evtFromDb.creatorRef()).isNull();

            }
        });
    }

    @Test
    public void insertwithCreator(){
        running(fakeApplication(), new Runnable() {
            public void run() {
                Event.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Event.class, String.class));
                User.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("users"), User.class, String.class));

                //Test
                Event event = Event.event().name("a name");
                event.creator(User.user().email("toto@gmail.com")).save();
                assertThat(event.id()).isNotNull();

                Event evtFromDb = Event.read(event.id());
                assertThat(evtFromDb).isNotNull();
                assertThat(evtFromDb.name()).isEqualTo(event.name());
                assertThat(evtFromDb.description()).isNull();
                assertThat(evtFromDb.creator().email()).isEqualTo("toto@gmail.com");
                assertThat(evtFromDb.creatorRef()).isNotNull();
                assertThat(evtFromDb.creatorRef().getId()).isEqualTo(evtFromDb.creator().id());
                assertThat(evtFromDb.subscribers()).isNotNull();
                assertThat(evtFromDb.subscribers().size()).isEqualTo(1);
                Subscriber subscriber = evtFromDb.subscribers().iterator().next();
                assertThat(evtFromDb.creator().empty()).isFalse();
                assertThat(evtFromDb.creator().email()).isEqualTo(event.creator().email());
                assertThat(evtFromDb.creator().email()).isEqualTo(subscriber.email());
                assertThat(subscriber.userRef()).isNotNull();
                assertThat(evtFromDb.creatorRef().getId()).isEqualTo(subscriber.userRef().getId());

            }
        });
    }

    @Test
    public void insertwithCreatorAlreadyExisting(){
        running(fakeApplication(), new Runnable() {
            public void run() {
                Event.collection = JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Event.class, String.class);
                User.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("users"), User.class, String.class));

                User creator = User.user().email("toto@gmail.com").insert();
                //Test :
                Event event = Event.event().name("a name").creator(creator).save();
                assertThat(event.id()).isNotNull();

                Event evtFromDb = Event.read(event.id());
                assertThat(evtFromDb).isNotNull();
                assertThat(evtFromDb.name()).isEqualTo(event.name());
                assertThat(evtFromDb.description()).isNull();
                assertThat(evtFromDb.creator().empty()).isFalse();
                assertThat(evtFromDb.creatorRef()).isNotNull();
                assertThat(evtFromDb.subscribers()).isNotNull();
                assertThat(evtFromDb.subscribers().size()).isEqualTo(1);
                Subscriber subscriber = evtFromDb.subscribers().iterator().next();
                assertThat(evtFromDb.creator()).isNotNull();
                assertThat(evtFromDb.creator().email()).isEqualTo(event.creator().email());
                assertThat(evtFromDb.creator().email()).isEqualTo(subscriber.email());
                assertThat(subscriber.userRef()).isNotNull();
                assertThat(evtFromDb.creatorRef().getId()).isEqualTo(subscriber.userRef().getId());

            }
        });
    }

    @Test
    public void testCreateEventAndAddSubriber(){
        running(fakeApplication(), new Runnable() {
            public void run() {
                Event.collection = JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Event.class, String.class);
                User.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("users"), User.class, String.class));
                Event event = Event.event().name("a name");
                event.creator().email("toto@gmail.com");
                Subscriber subsc = Subscriber.subscriber()
                        .address(Address.address().description("An adress"))
                        .email("subs@test.com").locomotion(Locomotion.AUTOSTOP);
                event.addSubscriber(subsc);
                event.save();
                assertThat(event.id()).isNotNull();
                Event evtFromDb = Event.read(event.id());
                assertThat(evtFromDb.subscribers().size()).isEqualTo(2);

                int nbTest = 0;
                boolean creatorFinded = false;
                boolean subscFinded = false;
                for(Subscriber subscriber : evtFromDb.subscribers()){
                    assertThat(subscriber.userRef()).isNotNull();
                    assertThat(subscriber.user().email()).isEqualTo(subscriber.email());
                    if(subscriber.email().equals("toto@gmail.com")){
                        creatorFinded = true;
                    }else if(subscriber.email().equals("subs@test.com")){
                        assertThat(subscriber.locomotion()).isEqualTo(subsc.locomotion());
                        assertThat(subscriber.address().description()).isEqualTo(subsc.address().description());
                        subscFinded = true;
                    }
                    nbTest++;
                }
                assertThat(nbTest).isEqualTo(2);
                assertThat(creatorFinded).isTrue();
                assertThat(subscFinded).isTrue();
            }
        });
    }

    @Test
    public void testCreateEventAndAddSubriberTwice(){
        running(fakeApplication(), new Runnable() {
            public void run() {
                Event.collection = JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Event.class, String.class);
                User.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("users"), User.class, String.class));
                Event event = Event.event().name("a name");
                event.creator().email("toto@gmail.com");
                Subscriber subsc = Subscriber.subscriber()
                        .address(Address.address().description("An adress"))
                        .email("subs@test.com").locomotion(Locomotion.AUTOSTOP);
                event.addSubscriber(subsc).addSubscriber(subsc);
                event.save();
                assertThat(event.id()).isNotNull();
                Event evtFromDb = Event.read(event.id());
                assertThat(evtFromDb.subscribers().size()).isEqualTo(2);

                int nbTest = 0;
                boolean creatorFinded = false;
                boolean subscFinded = false;
                for(Subscriber subscriber : evtFromDb.subscribers()){
                    assertThat(subscriber.userRef()).isNotNull();
                    assertThat(subscriber.user().email()).isEqualTo(subscriber.email());
                    if(subscriber.email().equals("toto@gmail.com")){
                        creatorFinded = true;
                    }else if(subscriber.email().equals("subs@test.com")){
                        assertThat(subscriber.locomotion()).isEqualTo(subsc.locomotion());
                        assertThat(subscriber.address().description()).isEqualTo(subsc.address().description());
                        subscFinded = true;
                    }
                    nbTest++;
                }
                assertThat(nbTest).isEqualTo(2);
                assertThat(creatorFinded).isTrue();
                assertThat(subscFinded).isTrue();
            }
        });
    }

    @Test
    public void testAddAndMergeSubscriber(){
        running(fakeApplication(), new Runnable() {
            public void run() {
                Event.collection = JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Event.class, String.class);
                User.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("users"), User.class, String.class));
                Event event = Event.event().name("a name");
                Subscriber subsc = Subscriber.subscriber()
                        .address(Address.address().description("An adress"))
                        .email("subs@test.com").locomotion(Locomotion.AUTOSTOP);
                Subscriber subsc2 = Subscriber.subscriber()
                        .email("subs@test.com").locomotion(Locomotion.CAR);
                event.addSubscriber(subsc).addAndMergeSubscriber(subsc2);
                event.save();
                assertThat(event.id()).isNotNull();
                Event evtFromDb = Event.read(event.id());
                assertThat(evtFromDb.subscribers().size()).isEqualTo(1);
                Subscriber subscriber = evtFromDb.subscribers().iterator().next();
                assertThat(subscriber.userRef()).isNotNull();
                assertThat(subscriber.user().email()).isEqualTo(subscriber.email());
                assertThat(subscriber.locomotion()).isEqualTo(subsc2.locomotion());
                assertThat(subscriber.address().description()).isEqualTo(subsc.address().description());
            }
        });
    }

    @Test
    public void testAddAndReplaceSubscriber(){
        running(fakeApplication(), new Runnable() {
            public void run() {
                Event.collection = JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Event.class, String.class);
                User.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("users"), User.class, String.class));
                Event event = Event.event().name("a name");
                Subscriber subsc = Subscriber.subscriber()
                        .address(Address.address().description("An adress"))
                        .email("subs@test.com").locomotion(Locomotion.AUTOSTOP);
                Subscriber subsc2 = Subscriber.subscriber()
                        .email("subs@test.com").locomotion(Locomotion.CAR);
                event.addSubscriber(subsc).addAndReplaceSubscriber(subsc2);
                event.save();
                assertThat(event.id()).isNotNull();
                Event evtFromDb = Event.read(event.id());
                assertThat(evtFromDb.subscribers().size()).isEqualTo(1);
                Subscriber subscriber = evtFromDb.subscribers().iterator().next();
                assertThat(subscriber.userRef()).isNotNull();
                assertThat(subscriber.user().email()).isEqualTo(subscriber.email());
                assertThat(subscriber.locomotion()).isEqualTo(subsc2.locomotion());
                assertThat(subscriber.address().empty()).isTrue();
            }
        });
    }
}
