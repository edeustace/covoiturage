package models;

import models.enums.Locomotion;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 27/05/13
 * Time: 19:11
 * To change this template use File | Settings | File Templates.
 */
public class SubscriberTest {

    @Test
    public void testEquals(){
        Subscriber sub1 = Subscriber.subscriber().email("a@b.com");
        Subscriber sub2 = Subscriber.subscriber().email("a@b.com");
        assertThat(sub1.equals(sub2)).isTrue();
    }

    @Test
    public void testNotEquals(){
        Subscriber sub1 = Subscriber.subscriber().email("a@b.com");
        Subscriber sub3 = Subscriber.subscriber().email("c@b.com");
        assertThat(sub1.equals(sub3)).isFalse();
        assertThat(sub1.equals(new String())).isFalse();
    }

    public void testEmpty(){
        assertThat(Subscriber.subscriber().empty()).isTrue();
        assertThat(Subscriber.subscriber().email("a@t.com").empty()).isFalse();
        assertThat(Subscriber.subscriber().name("toto").empty()).isFalse();
        assertThat(Subscriber.subscriber().surname("toto").empty()).isFalse();
        assertThat(Subscriber.subscriber().locomotion(Locomotion.AUTOSTOP).empty()).isFalse();
        assertThat(Subscriber.subscriber().user(User.user()).empty()).isTrue();
    }

    @Test
    public void testMerge(){
        Subscriber sub1 = Subscriber.subscriber().email("a@b.com").locomotion(Locomotion.AUTOSTOP).surname("alex");
        Subscriber sub2 = Subscriber.subscriber().email("c@b.com").locomotion(Locomotion.CAR).surname("alex");
        sub1.merge(sub2);
        assertThat(sub1.email()).isEqualTo("c@b.com");
        assertThat(sub1.locomotion()).isEqualTo(Locomotion.CAR);
        assertThat(sub1.surname()).isEqualTo("alex");
    }


    @Test
    public void testMergeAllField(){
        Subscriber sub1 = Subscriber.subscriber()
                .email("a@b.com").locomotion(Locomotion.AUTOSTOP)
                .surname("alex")
                .address(Address.address().description("nantes").location(Location.location().lat("33.92").lng("45.12")))
                .user(User.user().email("a@b.com"));
        Subscriber sub2 = Subscriber.subscriber().email("c@b.com").locomotion(Locomotion.CAR).surname("alex");
        sub1.merge(sub2);
        assertThat(sub1.email()).isEqualTo("c@b.com");
        assertThat(sub1.locomotion()).isEqualTo(Locomotion.CAR);
        assertThat(sub1.surname()).isEqualTo("alex");
    }
}
