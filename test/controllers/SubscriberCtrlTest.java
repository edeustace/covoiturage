package controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import java.net.UnknownHostException;

import models.Address;
import models.Event;
import models.Location;
import models.Subscriber;
import models.User;
import models.enums.Locomotion;
import net.vz.mongodb.jackson.JacksonDBCollection;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.libs.WS;

import com.mongodb.DB;
import com.mongodb.Mongo;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 02/06/13
 * Time: 22:22
 * To change this template use File | Settings | File Templates.
 */
public class SubscriberCtrlTest {
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
    public void testCreateOk() {
        Event.collection = JacksonDBCollection.wrap(currentDataBase.getCollection("events"), Event.class, String.class);
        User.collection(JacksonDBCollection.wrap(currentDataBase.getCollection("users"), User.class, String.class));

        running(testServer(3333), new Runnable() {
            public void run() {
                Event event = Event.event()
                        .setName("the event").setCreator(User.user().setEmail("email@toto.com").setPassword("password"))
                        .setAddress(Address.address().setDescription("somme address").setLocation(Location.location().setLng("55").setLat("56")));

                event.save();

                Subscriber subscriber = Subscriber.subscriber()
                        .setAddress(Address.address()
                                .setDescription("somewhere")
                                .setLocation(Location.location().setLat("123").setLng("456")))
                        .setEmail("adelegue@hotmail.com").setLocomotion(Locomotion.CAR).setUser(User.user().setEmail("adelegue@hotmail.com").setPassword("password"));
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode node = objectMapper.convertValue(subscriber, JsonNode.class);
                WS.Response response = WS.url("http://localhost:3333/rest/events/"+event.getId()+"/subscribers/").post(node).get();
                //System.out.println(response.getBody());
                //JsonNode resp = null;
                //try {
                //    resp = objectMapper.readValue(response.getBody(), JsonNode.class);
                //} catch (IOException e) {}
                assertThat(response.getStatus()).isEqualTo(OK);
                //WS.Response read = WS.url("http://localhost:3333/rest/events/"+event.getId()).get().get();
                //System.out.println(read.getBody());
                assertThat(response.getStatus()).isEqualTo(OK);
            }
        });
    }

}
