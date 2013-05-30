package models;

import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;
/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 30/05/13
 * Time: 21:22
 * To change this template use File | Settings | File Templates.
 */
public class UserTest {
    @Test
    public void testEmptyTrue(){
        assertThat(User.user().isEmpty()).isTrue();
    }
    @Test
    public void testEmptyFalse(){
        assertThat(User.user().setName("name").isEmpty()).isFalse();
        assertThat(User.user().setSurname("surname").isEmpty()).isFalse();
        assertThat(User.user().setEmail("email").isEmpty()).isFalse();
        assertThat(User.user().setPassword("pwd").isEmpty()).isFalse();
        assertThat(User.user().setAddress(Address.address().setDescription("address")).isEmpty()).isFalse();

    }
    @Test
    public void testMergeTotal(){
        User user = User.user().setName("name").setSurname("surname").setEmail("email").setPassword("pwd").setAddress(Address.address().setDescription("address"));
        User user2 = User.user().setName("name2").setSurname("surname2").setEmail("email2").setPassword("pwd2").setAddress(Address.address().setDescription("address2"));
        user.merge(user2);
        assertThat(user.getAddress().getDescription()).isEqualTo("address2");
        assertThat(user.getName()).isEqualTo("name2");
        assertThat(user.getSurname()).isEqualTo("surname2");
        assertThat(user.getEmail()).isEqualTo("email2");
        assertThat(user.getPassword()).isEqualTo("pwd2");
    }

    @Test
    public void testMergePartial(){
        User user = User.user().setName("name").setSurname("surname").setEmail("email").setPassword("pwd").setAddress(Address.address().setDescription("address"));
        User user2 = User.user().setSurname("surname2").setEmail("email2").setPassword("pwd2").setAddress(Address.address().setDescription("address2"));
        user.merge(user2);
        assertThat(user.getAddress().getDescription()).isEqualTo("address2");
        assertThat(user.getName()).isEqualTo("name");
        assertThat(user.getSurname()).isEqualTo("surname2");
        assertThat(user.getEmail()).isEqualTo("email2");
        assertThat(user.getPassword()).isEqualTo("pwd2");

        user = User.user().setName("name").setSurname("surname").setEmail("email").setPassword("pwd").setAddress(Address.address().setDescription("address"));
        user2 = User.user().setName("name2").setEmail("email2").setPassword("pwd2").setAddress(Address.address().setDescription("address2"));
        user.merge(user2);
        assertThat(user.getAddress().getDescription()).isEqualTo("address2");
        assertThat(user.getName()).isEqualTo("name2");
        assertThat(user.getSurname()).isEqualTo("surname");
        assertThat(user.getEmail()).isEqualTo("email2");
        assertThat(user.getPassword()).isEqualTo("pwd2");

        user = User.user().setName("name").setSurname("surname").setEmail("email").setPassword("pwd").setAddress(Address.address().setDescription("address"));
        user2 = User.user().setName("name2").setSurname("surname2").setPassword("pwd2").setAddress(Address.address().setDescription("address2"));
        user.merge(user2);
        assertThat(user.getAddress().getDescription()).isEqualTo("address2");
        assertThat(user.getName()).isEqualTo("name2");
        assertThat(user.getSurname()).isEqualTo("surname2");
        assertThat(user.getEmail()).isEqualTo("email");
        assertThat(user.getPassword()).isEqualTo("pwd2");

        user = User.user().setName("name").setSurname("surname").setEmail("email").setPassword("pwd").setAddress(Address.address().setDescription("address"));
        user2 = User.user().setName("name2").setSurname("surname2").setEmail("email2").setAddress(Address.address().setDescription("address2"));
        user.merge(user2);
        assertThat(user.getAddress().getDescription()).isEqualTo("address2");
        assertThat(user.getName()).isEqualTo("name2");
        assertThat(user.getSurname()).isEqualTo("surname2");
        assertThat(user.getEmail()).isEqualTo("email2");
        assertThat(user.getPassword()).isEqualTo("pwd");

        user = User.user().setName("name").setSurname("surname").setEmail("email").setPassword("pwd").setAddress(Address.address().setDescription("address"));
        user2 = User.user().setName("name2").setSurname("surname2").setEmail("email2").setPassword("pwd2");
        user.merge(user2);
        assertThat(user.getAddress().getDescription()).isEqualTo("address");
        assertThat(user.getName()).isEqualTo("name2");
        assertThat(user.getSurname()).isEqualTo("surname2");
        assertThat(user.getEmail()).isEqualTo("email2");
        assertThat(user.getPassword()).isEqualTo("pwd2");

    }
}


