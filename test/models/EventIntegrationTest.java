package models;


import commons.AbstractIntegrationTest;

import models.enums.Locomotion;
import org.junit.*;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Collections.found;
import static play.test.Helpers.*;

public class EventIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void simpleInsert(){
        running(fakeApplication(), new Runnable() {
            public void run() {

                Event event = Event.event().setName("a getName").save();
                assertThat(event.getId()).isNotNull();

                Event evtFromDb = Event.read(event.getId());
                assertThat(evtFromDb).isNotNull();
                assertThat(evtFromDb.getName()).isEqualTo(event.getName());
                assertThat(evtFromDb.getDescription()).isNull();
                assertThat(evtFromDb.getCreator()).isNull();
                assertThat(evtFromDb.getCreatorRef()).isNull();

            }
        });
    }

    @Test
    public void testOneInvitation(){
        running(fakeApplication(), new Runnable() {
            public void run() {

                Event event = Event.event().setName("a getName").setContacts(Arrays.asList("email1@toto.com", "email2@toto.com")).save();
                assertThat(event.getId()).isNotNull();

                List<Event> events = Event.listInvitedByEmail("email1@toto.com");
                assertThat(events).isNotEmpty();
                assertThat(events.size()).isEqualTo(1);
                found(events.iterator().next().getContacts(), "email1@toto.com");
            }
        });
    }

    @Test
    public void testSomeInvitations(){
        running(fakeApplication(), new Runnable() {
            public void run() {

                Event event = Event.event().setName("a name1").setContacts(Arrays.asList("email1@toto.com", "email2@toto.com")).save();
                assertThat(event.getId()).isNotNull();
                Event event2 = Event.event().setName("a name2").setContacts(Arrays.asList("email1@toto.com", "email6@toto.com")).save();
                assertThat(event2.getId()).isNotNull();
                Event event3 = Event.event().setName("a name3").setContacts(Arrays.asList("email1@toto.com", "email7@toto.com")).save();
                assertThat(event3.getId()).isNotNull();
                Event event4 = Event.event().setName("a name4").setContacts(Arrays.asList("email2@toto.com", "email7@toto.com")).save();
                assertThat(event4.getId()).isNotNull();

                List<Event> events = Event.listInvitedByEmail("email1@toto.com");
                assertThat(events).isNotEmpty();
                assertThat(events.size()).isEqualTo(3);
            }
        });
    }


    /**
     * Test with a getCreator. The getCreator should be added as subscriber with the good DBRef.
     */
    @Test
    public void insertwithCreator(){
        running(fakeApplication(), new Runnable() {
            public void run() {

                //Test
                Event event = Event.event().setName("a getName");
                event.setCreator(User.user().setEmail("toto@gmail.com")).save();
                assertThat(event.getId()).isNotNull();

                Event evtFromDb = Event.read(event.getId());
                assertThat(evtFromDb).isNotNull();
                assertThat(evtFromDb.getName()).isEqualTo(event.getName());
                assertThat(evtFromDb.getDescription()).isNull();
                assertThat(evtFromDb.getCreator()).isNull();
                assertThat(evtFromDb.getCreatorRef()).isNotNull();
                User creator = evtFromDb.loadCreator();
                assertThat(creator.getEmail()).isEqualTo("toto@gmail.com");
                assertThat(evtFromDb.getSubscribers()).isNotNull();
                assertThat(evtFromDb.getSubscribers().size()).isEqualTo(1);
                Subscriber subscriber = evtFromDb.getSubscribers().iterator().next();
                assertThat(subscriber.getUserRef()).isNotNull();
                assertThat(creator.getEmail()).isEqualTo("toto@gmail.com");
                assertThat(creator.getEmail()).isEqualTo(subscriber.getEmail());
                assertThat(subscriber.getUserRef()).isNotNull();
                assertThat(evtFromDb.getCreatorRef()).isEqualTo(subscriber.getUserRef());

            }
        });
    }

    /**
     * Test with a getCreator already existing in DB. The getCreator should be added as subscriber with the good DBRef.
     */
    @Test
    public void insertwithCreatorAlreadyExisting(){
        running(fakeApplication(), new Runnable() {
            public void run() {

                User creator = User.user().setEmail("toto@gmail.com").save();
                //Test :
                Event event = Event.event().setName("a getName").setCreator(creator).save();
                assertThat(event.getId()).isNotNull();

                Event evtFromDb = Event.read(event.getId());
                assertThat(evtFromDb).isNotNull();
                assertThat(evtFromDb.getName()).isEqualTo(event.getName());
                assertThat(evtFromDb.getDescription()).isNull();
                User creatorFromDb = evtFromDb.loadCreator();
                assertThat(creatorFromDb.isEmpty()).isFalse();
                assertThat(evtFromDb.getCreatorRef()).isNotNull();
                assertThat(evtFromDb.getSubscribers()).isNotNull();
                assertThat(evtFromDb.getSubscribers().size()).isEqualTo(1);
                Subscriber subscriber = evtFromDb.getSubscribers().iterator().next();
                assertThat(subscriber.getUserRef()).isNotNull();
                assertThat(creatorFromDb).isNotNull();
                assertThat(creatorFromDb.getEmail()).isEqualTo("toto@gmail.com");
                assertThat(creatorFromDb.getEmail()).isEqualTo(subscriber.getEmail());
                assertThat(subscriber.getUserRef()).isNotNull();
                assertThat(evtFromDb.getCreatorRef()).isEqualTo(subscriber.getUserRef());

            }
        });
    }

    /**
     * Test the add of a subscriber. The subscriber should be link to a getUser with a dbref. If the getUser does not exist, the getUser is created
     */
    @Test
    public void testCreateEventAndAddSubriber(){
        running(fakeApplication(), new Runnable() {
            public void run() {
                Event event = Event.event().setName("a getName");
                event.setCreator(User.user().setEmail("toto@gmail.com"));
                Subscriber subsc = Subscriber.subscriber()
                        .setAddress(Address.address().setDescription("An adress"))
                        .setEmail("subs@test.com").setLocomotion(Locomotion.AUTOSTOP);
                event.addSubscriber(subsc);
                event.save();
                assertThat(event.getId()).isNotNull();
                Event evtFromDb = Event.read(event.getId());
                assertThat(evtFromDb.getSubscribers().size()).isEqualTo(2);

                int nbTest = 0;
                boolean creatorFinded = false;
                boolean subscFinded = false;
                for(Subscriber subscriber : evtFromDb.getSubscribers()){
                    assertThat(subscriber.getUserRef()).isNotNull();
                    assertThat(subscriber.getUserRef()).isNotNull();
                    assertThat(User.findById(subscriber.getUserRef())).isNotNull();
                    assertThat(subscriber.getUser().getEmail()).isEqualTo(subscriber.getEmail());
                    if(subscriber.getEmail().equals("toto@gmail.com")){
                        creatorFinded = true;
                    }else if(subscriber.getEmail().equals("subs@test.com")){
                        assertThat(subscriber.getLocomotion()).isEqualTo(subsc.getLocomotion());
                        assertThat(subscriber.getAddress().getDescription()).isEqualTo(subsc.getAddress().getDescription());
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

    /**
     * If we add twice, the second should not be added
     */
    @Test
    public void testCreateEventAndAddSubriberTwice(){
        running(fakeApplication(), new Runnable() {
            public void run() {
                Event event = Event.event().setName("a getName");
                event.setCreator(User.user().setEmail("toto@gmail.com"));
                Subscriber subsc = Subscriber.subscriber()
                        .setAddress(Address.address().setDescription("An adress"))
                        .setEmail("subs@test.com").setLocomotion(Locomotion.AUTOSTOP);
                Subscriber subsc2 = Subscriber.subscriber()
                        .setAddress(Address.address().setDescription("An adress"))
                        .setEmail("subs@test.com").setLocomotion(Locomotion.CAR);
                event.addSubscriber(subsc).addSubscriber(subsc2);
                event.save();
                assertThat(event.getId()).isNotNull();
                Event evtFromDb = Event.read(event.getId());
                assertThat(evtFromDb.getSubscribers().size()).isEqualTo(2);

                int nbTest = 0;
                boolean creatorFinded = false;
                boolean subscFinded = false;
                for(Subscriber subscriber : evtFromDb.getSubscribers()){
                    assertThat(subscriber.getUserRef()).isNotNull();
                    assertThat(subscriber.getUserRef()).isNotNull();
                    assertThat(User.findById(subscriber.getUserRef())).isNotNull();
                    assertThat(subscriber.getUser().getEmail()).isEqualTo(subscriber.getEmail());
                    if(subscriber.getEmail().equals("toto@gmail.com")){
                        creatorFinded = true;
                    }else if(subscriber.getEmail().equals("subs@test.com")){
                        assertThat(subscriber.getLocomotion()).isEqualTo(subsc.getLocomotion());
                        assertThat(subscriber.getAddress().getDescription()).isEqualTo(subsc.getAddress().getDescription());
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

    /**
     * Add merge method should merge the not null fields
     */
    @Test
    public void testAddAndMergeSubscriber(){
        running(fakeApplication(), new Runnable() {
            public void run() {
                Event event = Event.event().setName("a getName");
                Subscriber subsc = Subscriber.subscriber()
                        .setAddress(Address.address().setDescription("An adress"))
                        .setEmail("subs@test.com").setLocomotion(Locomotion.AUTOSTOP);
                Subscriber subsc2 = Subscriber.subscriber()
                        .setEmail("subs@test.com").setLocomotion(Locomotion.CAR);
                event.addSubscriber(subsc).addAndMergeSubscriber(subsc2);
                event.save();
                assertThat(event.getId()).isNotNull();
                Event evtFromDb = Event.read(event.getId());
                assertThat(evtFromDb.getSubscribers().size()).isEqualTo(1);
                Subscriber subscriber = evtFromDb.getSubscribers().iterator().next();
                assertThat(subscriber.getUserRef()).isNotNull();
                assertThat(subscriber.getUser().getEmail()).isEqualTo(subsc.getEmail());
                assertThat(subscriber.getLocomotion()).isEqualTo(subsc2.getLocomotion());
                assertThat(subscriber.getAddress().getDescription()).isEqualTo(subsc.getAddress().getDescription());
            }
        });
    }


    @Test
    public void testAddAndReplaceSubscriber(){
        running(fakeApplication(), new Runnable() {
            public void run() {
                Event event = Event.event().setName("a getName");
                Subscriber subsc = Subscriber.subscriber()
                        .setAddress(Address.address().setDescription("An adress"))
                        .setEmail("subs@test.com").setLocomotion(Locomotion.AUTOSTOP);
                Subscriber subsc2 = Subscriber.subscriber()
                        .setEmail("subs@test.com").setLocomotion(Locomotion.CAR);
                event.addSubscriber(subsc).addAndReplaceSubscriber(subsc2);
                event.save();
                assertThat(event.getId()).isNotNull();
                Event evtFromDb = Event.read(event.getId());
                assertThat(evtFromDb.getSubscribers().size()).isEqualTo(1);
                Subscriber subscriber = evtFromDb.getSubscribers().iterator().next();
                assertThat(subscriber.getUserRef()).isNotNull();
                assertThat(subscriber.getUserRef()).isNotNull();
                assertThat(subscriber.getUser().getEmail()).isEqualTo(subscriber.getEmail());
                assertThat(subscriber.getLocomotion()).isEqualTo(subsc2.getLocomotion());
                assertThat(subscriber.getAddress()).isNull();
            }
        });
    }

//    private void validateSubscriber(Subscriber subscriber, Subscriber expected){
//        assertThat(subscriber.getUserRef()).isNotNull();
//        assertThat(User.findById(subscriber.getUserRef())).isNotNull();
//        assertThat(subscriber.getUser().getEmail()).isEqualTo(expected.getEmail());
//        assertThat(subscriber.getLocomotion()).isEqualTo(expected.getLocomotion());
//        assertThat(subscriber.getAddress()).isEqualTo(expected);
//    }
}
