package controllers;

import com.mongodb.DB;
import com.mongodb.Mongo;
import models.*;
import net.vz.mongodb.jackson.JacksonDBCollection;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.WS;

import java.io.IOException;
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
    public void testCreateWithErrors() {
        Event.collection = JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Event.class, String.class);
        User.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("users"), User.class, String.class));

        running(testServer(3333), new Runnable() {
            public void run() {

                Event event = Event.event().setCreator(User.user().setEmail("toto@gmail.com"));
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode node = objectMapper.convertValue(event, JsonNode.class);
                WS.Response response = WS.url("http://localhost:3333/rest/events").post(node).get();
                System.out.println(response.getStatus());
                System.out.println(response.getBody());
                JsonNode resp = null;
                try {
                    resp = objectMapper.readValue(response.getBody(), JsonNode.class);
                }catch (IOException e){};
                assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
                assertThat(resp.get("name")).isNotEmpty();
                assertThat(resp.get("address")).isNotEmpty();
                assertThat(resp.get("creator.password")).isNotEmpty();
                assertThat(resp.get("name")).isNotEmpty();
            }
        });
    }

    @Test
    public void testKoUserAlreadyExisting() {
        Event.collection = JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Event.class, String.class);
        User.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("users"), User.class, String.class));

        running(testServer(3333), new Runnable() {
            public void run() {
                User.user().setEmail("email@toto.com").save();
                Event event = Event.event()
                        .setName("the event").setCreator(User.user().setEmail("email@toto.com").setPassword("password"))
                        .setAddress(Address.address()
                                .setDescription("somme address")
                                .setLocation(Location.location().setLng("55").setLat("56")));
                //event.getSubscribers().add(Subscriber.subscriber().setEmail("toto@test.fr"));
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode node = objectMapper.convertValue(event, JsonNode.class);
                WS.Response response = WS.url("http://localhost:3333/rest/events").post(node).get();
                JsonNode resp = null;
                try {
                    resp = objectMapper.readValue(response.getBody(), JsonNode.class);
                } catch (IOException e) {}
                assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
                assertThat(resp.get("creator.email")).isNotNull();

            }
        });
    }


    @Test
    public void testCreateOk() {
        Event.collection = JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Event.class, String.class);
        User.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("users"), User.class, String.class));

        running(testServer(3333), new Runnable() {
            public void run() {
                Event event = Event.event()
                        .setName("the event").setCreator(User.user().setEmail("email@toto.com").setPassword("password"))
                        .setAddress(Address.address().setDescription("somme address").setLocation(Location.location().setLng("55").setLat("56")));
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode node = objectMapper.convertValue(event, JsonNode.class);
                WS.Response response = WS.url("http://localhost:3333/rest/events").post(node).get();
                //System.out.println(response.getStatus());
                //System.out.println(response.getBody());
                JsonNode resp = null;
                try {
                    resp = objectMapper.readValue(response.getBody(), JsonNode.class);
                } catch (IOException e) {}
                assertThat(response.getStatus()).isEqualTo(OK);
                assertThat(resp.get("name").getTextValue()).isEqualTo(event.getName());
                WS.Response read = WS.url("http://localhost:3333"+resp.get("link").get("href").getTextValue()).get().get();
                assertThat(response.getStatus()).isEqualTo(OK);
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
                //System.out.println(response.getBody());
            }
        });
    }
}
