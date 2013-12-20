package models;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import java.net.UnknownHostException;

import commons.AbstractIntegrationTest;
import net.vz.mongodb.jackson.JacksonDBCollection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.mongodb.DB;
import com.mongodb.Mongo;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 30/05/13
 * Time: 21:46
 * To change this template use File | Settings | File Templates.
 */
public class UserIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void simpleInsert(){
        running(fakeApplication(), new Runnable() {
            public void run() {

                User user = User.user().setName("a getName").save();
                assertThat(user.getId()).isNotNull();

                User userFromDb = User.findById(user.getId());
                assertThat(userFromDb).isNotNull();
                assertThat(userFromDb.getName()).isEqualTo(user.getName());
                assertThat(userFromDb.getPassword()).isNull();
                assertThat(userFromDb.getSurname()).isNull();
                assertThat(userFromDb.getEmail()).isNull();
                assertThat(userFromDb.getAddress()).isNull();
            }
        });
    }
    
    @Test
    public void findByAuthUserIdentity(){
        running(fakeApplication(), new Runnable() {
            public void run() {

                User user = User.user().setName("a getName").setEmail("email@toto.com");
                user.getLinkedAccounts().add(LinkedAccount.linkedAccount().setId("123456").setProviderKey("larouss").setProviderUserId("654321"));
                user.getLinkedAccounts().add(LinkedAccount.linkedAccount().setId("789456").setProviderKey("yoyoyoy").setProviderUserId("645977"));
                user.setActive(true);
                user.save();
                assertThat(user.getId()).isNotNull();
                
                AuthUserIdentity identity = new AuthUserIdentityMock("654321", "larouss") ;
                
                User userFromDb = User.findByAuthUserIdentity(identity);
                assertThat(userFromDb).isNotNull();
                assertThat(userFromDb.getName()).isEqualTo(user.getName());
                assertThat(userFromDb.getEmail()).isEqualTo(user.getEmail());
            }
        });
    }
    
    @Test
    public void findByAuthUserIdentityWithUsernamePasswordAuthUser(){
        running(fakeApplication(), new Runnable() {
            public void run() {

                User user = User.user().setName("a getName").setEmail("email@toto.com");
                user.getLinkedAccounts().add(LinkedAccount.linkedAccount().setId("123456").setProviderKey("larouss").setProviderUserId("654321"));
                user.getLinkedAccounts().add(LinkedAccount.linkedAccount().setId("789456").setProviderKey("password").setProviderUserId("654321"));
                user.setActive(true);
                user.save();
                assertThat(user.getId()).isNotNull();
             
                UsernamePasswordAuthUser identity = new UsernamePasswordAuthUserMock("654321", "email@toto.com") ;
                
                User userFromDb = User.findByAuthUserIdentity(identity);
                assertThat(userFromDb).isNotNull();
                assertThat(userFromDb.getName()).isEqualTo(user.getName());
                assertThat(userFromDb.getEmail()).isEqualTo(user.getEmail());
            }
        });
    }    
    
	@Test
	public void testAddLinkedAccount() throws Exception {
		running(fakeApplication(), new Runnable() {
            public void run() {

                User user = User.user().setName("a getName").setEmail("email@toto.com");
                user.getLinkedAccounts().add(LinkedAccount.linkedAccount().setId("123456").setProviderKey("larouss").setProviderUserId("654321"));
                user.setActive(true);
                user.save();
                assertThat(user.getId()).isNotNull();
                
                AuthUser linked = new UsernamePasswordAuthUserMock("654321", "email@toto.com") ;
                AuthUser current = new AuthUserIdentityMock("654321", "larouss") ;
                
                User.addLinkedAccount(current, linked);
                
                User userFromDb = User.findById(user.getId());
                assertThat(userFromDb).isNotNull();
                assertThat(userFromDb.getName()).isEqualTo(user.getName());
                assertThat(userFromDb.getEmail()).isEqualTo(user.getEmail());
                assertThat(userFromDb.getLinkedAccounts().size()).isEqualTo(2);
            }
        });
	}
  
	@Test
	public void existsByAuthUserIdentity(){
		running(fakeApplication(), new Runnable() {
            public void run() {

                User user = User.user().setName("a getName").setEmail("email@toto.com");
                user.getLinkedAccounts().add(LinkedAccount.linkedAccount().setId("123456").setProviderKey("larouss").setProviderUserId("654321"));
                user.setActive(true);
                user.save();
                assertThat(user.getId()).isNotNull();
                
                AuthUser current = new AuthUserIdentityMock("654321", "larouss") ;
                Boolean exist = User.existsByAuthUserIdentity(current);
                assertThat(exist).isTrue();
                AuthUser other = new UsernamePasswordAuthUserMock("654321", "email@toto.com") ;
                exist = User.existsByAuthUserIdentity(other);
                assertThat(exist).isFalse();
            }
        });
	}
	
	@Test 
	public void createNonExisting(){
		running(fakeApplication(), new Runnable() {
            public void run() {

                AuthUser current = new AuthUserIdentityMock("654321", "larouss") ;
                User.create(current);
                
                Boolean exist = User.existsByAuthUserIdentity(current);
                assertThat(exist).isTrue();
            }
        });
	}
	
	@Test 
	public void createExisting(){
		running(fakeApplication(), new Runnable() {
            public void run() {

                User user = User.user().setName("a getName").setEmail("email@toto.com");
                user.getLinkedAccounts().add(LinkedAccount.linkedAccount().setId("123456").setProviderKey("larouss").setProviderUserId("654321"));
                user.setActive(true);
                user.save();
                assertThat(user.getId()).isNotNull();
                
                
                AuthUser current = new AuthUserIdentityMock("654321", "larouss") ;
                User.create(current);
                
                User userFromDb = User.findById(user.getId());
                assertThat(userFromDb).isNotNull();
                assertThat(userFromDb.getName()).isEqualTo(user.getName());
                assertThat(userFromDb.getEmail()).isEqualTo(user.getEmail());
                assertThat(userFromDb.getLinkedAccounts().size()).isEqualTo(1);
            }
        });
	}
	
	@Test 
	public void createExistingWithNewIdentity(){
		running(fakeApplication(), new Runnable() {
            public void run() {

                User user = User.user().setName("a getName").setEmail("email@toto.com");
                user.getLinkedAccounts().add(LinkedAccount.linkedAccount().setId("123456").setProviderKey("larouss").setProviderUserId("654321"));
                user.setActive(true);
                user.save();
                assertThat(user.getId()).isNotNull();
                
                
                AuthUser current = new UsernamePasswordAuthUserMock("456789", "email@toto.com") ;
                User.create(current);
                
                User userFromDb = User.findById(user.getId());
                assertThat(userFromDb).isNotNull();
                assertThat(userFromDb.getName()).isEqualTo(user.getName());
                assertThat(userFromDb.getEmail()).isEqualTo(user.getEmail());
                assertThat(userFromDb.getLinkedAccounts().size()).isEqualTo(2);
                
            }
        });
	}
	@Test 
	public void createExistingWithWrongMail(){
		running(fakeApplication(), new Runnable() {
            public void run() {

                User user = User.user().setName("a getName").setEmail("email@toto.com");
                user.getLinkedAccounts().add(LinkedAccount.linkedAccount().setId("123456").setProviderKey("larouss").setProviderUserId("654321"));
                user.setActive(true);
                user.save();
                assertThat(user.getId()).isNotNull();
                
                
                AuthUser current = new UsernamePasswordAuthUserMock("456789", "adelegue@toto.com") ;
                User.create(current);
                
                User userFromDb = User.getUserwithEmail("adelegue@toto.com");
                assertThat(userFromDb).isNotNull();
                assertThat(userFromDb.getName()).isNull();
                assertThat(userFromDb.getEmail()).isEqualTo("adelegue@toto.com");
                assertThat(userFromDb.getEmailValidated()).isTrue();
                assertThat(userFromDb.getLinkedAccounts().size()).isEqualTo(1);
                
            }
        });
	}
    @SuppressWarnings("serial")
	public static class UsernamePasswordAuthUserMock extends UsernamePasswordAuthUser{
		public UsernamePasswordAuthUserMock(String clearPassword, String email) {
			super(clearPassword, email);
		}
    }
    
    @SuppressWarnings("serial")
    public static class AuthUserIdentityMock extends AuthUser implements AuthUserIdentity{
    	private String id; 
    	private String provider; 
    	
		public AuthUserIdentityMock(String id, String provider) {
			super();
			this.id = id;
			this.provider = provider;
		}
		@Override
		public String getId() {
			return id;
		}
		@Override
		public String getProvider() {
			return provider;
		}
    }
    
}
