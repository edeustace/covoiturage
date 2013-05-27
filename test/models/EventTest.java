package models;


import com.mongodb.DB;
import com.mongodb.Mongo;
import models.enums.Locomotion;
import net.vz.mongodb.jackson.JacksonDBCollection;
import org.junit.*;

import java.net.UnknownHostException;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class EventTest {


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

                Event event = Event.newOne().name("a name").insert();
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
                Event event = Event.newOne().name("a name");
                event.creator(User.user().email("toto@gmail.com")).insert();
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
                Event event = Event.newOne().name("a name").creator(creator).insert();
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
                Event event = Event.newOne().name("a name");
                event.creator().email("toto@gmail.com");
                event.addSubscriber(Subscriber.subscriber()
                           .address(Address.address().description("An adress"))
                            .email("subs@test.com").locomotion(Locomotion.AUTOSTOP));
                event.insert();
                assertThat(event.id()).isNotNull();
                Event evtFromDb = Event.read(event.id());
                assertThat(evtFromDb.subscribers().size()).isEqualTo(2);
            }
        });
    }

}
