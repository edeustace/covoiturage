package controllers;

import com.mongodb.DB;
import com.mongodb.Mongo;
import models.Event;
import models.User;
import net.vz.mongodb.jackson.JacksonDBCollection;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.WS;

import java.net.UnknownHostException;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 30/05/13
 * Time: 21:49
 * To change this template use File | Settings | File Templates.
 */
public class EventCtrlTest {

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
    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */
    @Test
    public void test() {
        Event.collection = JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Event.class, String.class);
        User.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("users"), User.class, String.class));

        running(testServer(3333), new Runnable() {
            public void run() {
                Event event = Event.event().setName("event").setCreator(User.user().setEmail("email@toto.com"));
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode node = objectMapper.convertValue(event, JsonNode.class);
                WS.Response response = WS.url("http://localhost:3333/rest/events").post(node).get();
                System.out.println(response.getStatus());
                System.out.println(response.getBody());
            }
        });
    }

    @Test
    public void testGet() {
        Event.collection = JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Event.class, String.class);
        User.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("users"), User.class, String.class));

        running(testServer(3333), new Runnable() {
            public void run() {
                Event event = Event.event().setName("event").setCreator(User.user().setEmail("email@toto.com"));
                event.save();
                WS.Response response = WS.url("http://localhost:3333/rest/events/"+event.getId()).get().get();
                assertThat(response.getStatus()).isEqualTo(OK);
                System.out.println(response.getBody());
            }
        });
    }
}
