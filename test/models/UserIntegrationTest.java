package models;

import com.mongodb.DB;
import com.mongodb.Mongo;
import net.vz.mongodb.jackson.JacksonDBCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 30/05/13
 * Time: 21:46
 * To change this template use File | Settings | File Templates.
 */
public class UserIntegrationTest {
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
                User.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("users"), User.class, String.class));

                User user = User.user().setName("a getName").save();
                assertThat(user.id()).isNotNull();

                User userFromDb = User.findById(user.id());
                assertThat(userFromDb).isNotNull();
                assertThat(userFromDb.getName()).isEqualTo(user.getName());
                assertThat(userFromDb.getPassword()).isNull();
                assertThat(userFromDb.getSurname()).isNull();
                assertThat(userFromDb.getEmail()).isNull();
                assertThat(userFromDb.getAddress()).isNull();
            }
        });
    }
}
