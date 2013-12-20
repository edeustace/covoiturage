package controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.AbstractIntegrationTest;
import controllers.decorators.EventLight;
import models.Event;
import models.User;
import org.junit.Test;
import play.libs.WS;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import play.Logger;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

/**
 * Created by adelegue on 20/12/2013.
 */
public class UserCtrlTest extends AbstractIntegrationTest {

    @Test
    public void testListInvitation(){
        running(testServer(3333), new Runnable() {
            public void run() {

                Event event = Event.event().setName("a name1").setContacts(Arrays.asList("email1@toto.com", "email2@toto.com")).save();
                assertThat(event.getId()).isNotNull();
                Event event2 = Event.event().setName("a name2").setContacts(Arrays.asList("email1@toto.com", "email6@toto.com")).save();
                assertThat(event2.getId()).isNotNull();
                Event event3 = Event.event().setName("a name3").setContacts(Arrays.asList("email1@toto.com", "email7@toto.com")).save();
                assertThat(event3.getId()).isNotNull();
                Event event4 = Event.event().setName("a name4").setContacts(Arrays.asList("email2@toto.com", "email7@toto.com")).save();
                assertThat(event4.getId()).isNotNull();

                User user = User.user().setName("a getName").setEmail("email1@toto.com").save();
                assertThat(user.getId()).isNotNull();


                String url =  "http://localhost:3333/rest/users/"+user.getId()+"/invitations";
                WS.Response response = WS.url(url).get().get(10000);
                System.out.println(response.getBody());

                assertThat(response.getStatus()).isEqualTo(OK);
                ObjectMapper objectMapper = new ObjectMapper();
                List<Event> events = objectMapper.convertValue(response.asJson(), new TypeReference<List<Event>>() {
                    @Override
                    public Type getType() {
                        return super.getType();
                    }
                });
                assertThat(events).isNotEmpty();
                assertThat(events.size()).isEqualTo(3);
            }
        });
    }

}
