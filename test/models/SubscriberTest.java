package models;

import models.enums.Locomotion;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

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
        Subscriber sub1 = Subscriber.subscriber().setEmail("a@b.com");
        Subscriber sub2 = Subscriber.subscriber().setEmail("a@b.com");
        assertThat(sub1.equals(sub2)).isTrue();
    }

    @Test
    public void testNotEquals(){
        Subscriber sub1 = Subscriber.subscriber().setEmail("a@b.com");
        Subscriber sub3 = Subscriber.subscriber().setEmail("c@b.com");
        assertThat(sub1.equals(sub3)).isFalse();
        assertThat(sub1.equals(new String())).isFalse();
    }

    public void testEmpty(){
        assertThat(Subscriber.subscriber().isEmpty()).isTrue();
        assertThat(Subscriber.subscriber().setEmail("a@t.com").isEmpty()).isFalse();
        assertThat(Subscriber.subscriber().setName("toto").isEmpty()).isFalse();
        assertThat(Subscriber.subscriber().setSurname("toto").isEmpty()).isFalse();
        assertThat(Subscriber.subscriber().setLocomotion(Locomotion.AUTOSTOP).isEmpty()).isFalse();
        assertThat(Subscriber.subscriber().setUser(User.user()).isEmpty()).isTrue();
    }

    @Test
    public void testMerge(){
        Subscriber sub1 = Subscriber.subscriber().setEmail("a@b.com").setLocomotion(Locomotion.AUTOSTOP).setSurname("alex");
        Subscriber sub2 = Subscriber.subscriber().setEmail("c@b.com").setLocomotion(Locomotion.CAR).setSurname("alex");
        sub1.merge(sub2);
        assertThat(sub1.getEmail()).isEqualTo("c@b.com");
        assertThat(sub1.getLocomotion()).isEqualTo(Locomotion.CAR);
        assertThat(sub1.getSurname()).isEqualTo("alex");
    }


    @Test
    public void testMergeAllField(){
        Subscriber sub1 = Subscriber.subscriber()
                .setEmail("a@b.com").setLocomotion(Locomotion.AUTOSTOP)
                .setSurname("alex")
                .setAddress(Address.address().setDescription("nantes").setLocation(Location.location().setLat("33.92").setLng("45.12")))
                .setUser(User.user().setEmail("a@b.com"));
        Subscriber sub2 = Subscriber.subscriber().setEmail("c@b.com").setLocomotion(Locomotion.CAR).setSurname("alex");
        sub1.merge(sub2);
        assertThat(sub1.getEmail()).isEqualTo("c@b.com");
        assertThat(sub1.getLocomotion()).isEqualTo(Locomotion.CAR);
        assertThat(sub1.getSurname()).isEqualTo("alex");
    }

    @Test
    public void testMergeAllField2(){
        Subscriber sub1 = Subscriber.subscriber()
                .setEmail("a@b.com").setLocomotion(Locomotion.AUTOSTOP)
                .setSurname("alex")
                .setAddress(Address.address().setDescription("nantes").setLocation(Location.location().setLat("33.92").setLng("45.12")))
                .setUser(User.user().setEmail("a@b.com"));
        Subscriber sub2 = Subscriber.subscriber().setEmail("c@b.com").setLocomotion(Locomotion.CAR).setSurname("alex");
        sub1.merge(sub2);
        assertThat(sub1.getEmail()).isEqualTo("c@b.com");
        assertThat(sub1.getLocomotion()).isEqualTo(Locomotion.CAR);
        assertThat(sub1.getSurname()).isEqualTo("alex");
        sub2.setEmail("ggggg@b.com").setLocomotion(Locomotion.DONT_KNOW_YET).setSurname("alex123456");
        assertThat(sub1.getEmail()).isEqualTo("c@b.com");
        assertThat(sub1.getLocomotion()).isEqualTo(Locomotion.CAR);
        assertThat(sub1.getSurname()).isEqualTo("alex");
    }
}
