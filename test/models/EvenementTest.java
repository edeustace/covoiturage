package models;


import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import net.vz.mongodb.jackson.JacksonDBCollection;
import org.junit.*;

import play.modules.mongodb.jackson.MongoDB;
import play.mvc.*;
import play.test.*;
import play.libs.F.*;

import java.net.UnknownHostException;
import java.util.Date;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

public class EvenementTest {


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
                Evenement.collection = JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Evenement.class, String.class);
                Evenement evenement = new Evenement();
                evenement.name = "a name";
                evenement.insert();
                assertThat(evenement.id).isNotNull();

                Evenement evtFromDb = Evenement.read(evenement.id);
                assertThat(evtFromDb).isNotNull();
                assertThat(evtFromDb.name).isEqualTo(evenement.name);
                assertThat(evtFromDb.description).isNull();
                assertThat(evtFromDb.creator).isNull();
                assertThat(evtFromDb.creatorRef).isNull();

            }
        });
    }

    @Test
    public void insertwithCreator(){
        running(fakeApplication(), new Runnable() {
            public void run() {
                Evenement.collection = JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Evenement.class, String.class);
                Evenement evenement = new Evenement();
                evenement.name = "a name";
                evenement.creator = new User();
                evenement.creator.email = "toto@gmail.com";
                evenement.insert();
                assertThat(evenement.id).isNotNull();

                Evenement evtFromDb = Evenement.read(evenement.id);
                assertThat(evtFromDb).isNotNull();
                assertThat(evtFromDb.name).isEqualTo(evenement.name);
                assertThat(evtFromDb.description).isNull();
                assertThat(evtFromDb.creator).isNull();
                assertThat(evtFromDb.creatorRef).isNotNull();
                assertThat(evtFromDb.subscribers).isNotNull();
                assertThat(evtFromDb.subscribers.size()).isEqualTo(1);
                Subscriber subscriber = evtFromDb.subscribers.iterator().next();
                evtFromDb.getCreator();
                assertThat(evtFromDb.creator).isNotNull();
                assertThat(evtFromDb.creator.email).isEqualTo(evenement.creator.email);
                assertThat(evtFromDb.creator.email).isEqualTo(subscriber.email);
                assertThat(evtFromDb.creatorRef.getId()).isEqualTo(subscriber.userRef.getId());

            }
        });
    }

}
