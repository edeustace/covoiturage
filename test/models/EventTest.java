package models;

import models.enums.Locomotion;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 30/05/13
 * Time: 19:14
 * To change this template use File | Settings | File Templates.
 */
public class EventTest {

    //EMPTY
    @Test
    public void testEmpty(){
        assertThat(Event.event().isEmpty()).isTrue();
    }
    @Test
    public void testEmptyFalseBecauseOfName(){
        assertThat(Event.event().setName("getName").isEmpty()).isFalse();
    }
    @Test
    public void testEmptyFalseBecauseOfDesc(){
        assertThat(Event.event().setDescription("desc").isEmpty()).isFalse();
    }
    @Test
    public void testEmptyFalseBecauseOfAddress(){
        assertThat(Event.event().setAddress(Address.address().setDescription("desc")).isEmpty()).isFalse();
    }
    @Test
    public void testEmptyFalseBecauseOfCreator(){
        assertThat(Event.event().setCreator(User.user().setName("getName")).isEmpty()).isFalse();
    }
    @Test
    public void testEmptyFalseBecauseOfCreatorRef(){
        assertThat(Event.event().setCreatorRef("getId").isEmpty()).isFalse();
    }

    //ADD AND MERGE
    @Test
    public void testAddAndMergeOneElt(){
        Event event = Event.event();
        event.addAndMergeSubscriber(Subscriber.subscriber().setEmail("toto@getEmail.com"));
        assertThat(event.getSubscribers().size()).isEqualTo(1);
        assertThat(event.getSubscribers().iterator().next().getEmail()).isEqualTo("toto@getEmail.com");
    }
    @Test
    public void testAddAndMergeTwoDiffElt(){
        Event event = Event.event();
        event.addAndMergeSubscriber(Subscriber.subscriber().setEmail("toto@getEmail.com").setName("sub1"));
        event.addAndMergeSubscriber(Subscriber.subscriber().setEmail("tata@getEmail.com").setName("sub2"));
        assertThat(event.getSubscribers().size()).isEqualTo(2);
        for(Subscriber subs : event.getSubscribers()){
            if(subs.getEmail().equals("tata@getEmail.com")){
                assertThat(subs.getName()).isEqualTo("sub2");
            }else if(subs.getEmail().equals("toto@getEmail.com")){
                assertThat(subs.getName()).isEqualTo("sub1");
            }else{
               assertThat(false).isTrue();
            }
        }
    }
    @Test
    public void testAddAndMergeTwoElt(){
        Event event = Event.event();
        event.addAndMergeSubscriber(Subscriber.subscriber().setEmail("toto@getEmail.com").setName("sub1"));
        event.addAndMergeSubscriber(Subscriber.subscriber().setEmail("toto@getEmail.com").setName("sub2"));
        assertThat(event.getSubscribers().size()).isEqualTo(1);
        for(Subscriber subs : event.getSubscribers()){
            if(subs.getEmail().equals("toto@getEmail.com")){
                assertThat(subs.getName()).isEqualTo("sub2");
            }else{
                assertThat(false).isTrue();
            }
        }
    }

    //ADD AND REPLACE
    //ADD AND MERGE
    @Test
    public void testAddAndReplaceOneElt(){
        Event event = Event.event();
        event.addAndReplaceSubscriber(Subscriber.subscriber().setEmail("toto@getEmail.com"));
        assertThat(event.getSubscribers().size()).isEqualTo(1);
        assertThat(event.getSubscribers().iterator().next().getEmail()).isEqualTo("toto@getEmail.com");
    }
    @Test
    public void testAddAndReplaceTwoDiffElt(){
        Event event = Event.event();
        event.addAndReplaceSubscriber(Subscriber.subscriber().setEmail("toto@getEmail.com").setName("sub1"));
        event.addAndReplaceSubscriber(Subscriber.subscriber().setEmail("tata@getEmail.com").setName("sub2"));
        assertThat(event.getSubscribers().size()).isEqualTo(2);
        for(Subscriber subs : event.getSubscribers()){
            if(subs.getEmail().equals("tata@getEmail.com")){
                assertThat(subs.getName()).isEqualTo("sub2");
            }else if(subs.getEmail().equals("toto@getEmail.com")){
                assertThat(subs.getName()).isEqualTo("sub1");
            }else{
                assertThat(false).isTrue();
            }
        }
    }
    @Test
    public void testAddAndReplaceTwoElt(){
        Event event = Event.event();
        event.addAndReplaceSubscriber(Subscriber.subscriber().setEmail("toto@getEmail.com").setName("sub1").setLocomotion(Locomotion.CAR));
        event.addAndReplaceSubscriber(Subscriber.subscriber().setEmail("toto@getEmail.com").setName("sub2"));
        assertThat(event.getSubscribers().size()).isEqualTo(1);
        for(Subscriber subs : event.getSubscribers()){
            if(subs.getEmail().equals("toto@getEmail.com")){
                assertThat(subs.getName()).isEqualTo("sub2");
                assertThat(subs.getLocomotion()).isNull();
            }else{
                assertThat(false).isTrue();
            }
        }
    }

}
