package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import models.enums.Locomotion;
import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.validator.constraints.Email;

import play.data.format.Formats.DateTime;
import play.modules.mongodb.jackson.MongoDB;
import be.objectify.deadbolt.core.models.Subject;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;

@MongoCollection(name="users")
public class User implements Subject {
	
	private static JacksonDBCollection<User, String> collection = null;
	public static void collection(JacksonDBCollection<User, String> collection){
        User.collection = collection;
    }
    public static JacksonDBCollection<User, String> collection(){
        if(collection==null){
            collection = MongoDB.getCollection(User.class, String.class);
        }
        return collection;
    }

    @Override
    @JsonIgnore
    public String getIdentifier() {
        return id;
    }

    private String id;

    private String version = "1";
    
    @NotNull @Email
    private String email;

    private String password;

    private String name;

    private String surname;

    private Address address;

    private Boolean emailValidated;

    private Boolean active;
    
    @DateTime(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
    private Date lastLogin;
    
    private Locomotion locomotion;

    private List<String> contacts = new ArrayList<>();
    
	private List<LinkedAccount> linkedAccounts = new ArrayList<LinkedAccount>();

    private java.util.List<SecurityRole> roles = new ArrayList<SecurityRole>();

    private java.util.List<UserPermission> permissions = new ArrayList<UserPermission>();

    ///////////  CLASS METHODS /////////////////

    @JsonIgnore
    public Boolean isEmpty(){
        return id==null && email == null &&
                password==null && name==null &&
                surname == null && address==null;
    }

    public User merge(User user){
        if(user!= null && !user.isEmpty()){
            if(user.getEmail()!=null){
                this.setEmail(user.getEmail());
            }
            if(user.getName()!=null){
                this.setName(user.getName());
            }
            if(user.getSurname()!=null){
                this.setSurname(user.getSurname());
            }
            if(user.getPassword()!=null){
                this.setPassword(user.getPassword());
            }
            if(user.getAddress()!=null && !user.getAddress().empty()){
                if(this.getAddress()==null){
                    this.setAddress(Address.address());
                }
                this.getAddress().merge(user.getAddress());
            }
        }
        return this;
    }
    public User mergeIfNull(User user){
        if(user!= null && !user.isEmpty()){
            if(this.getEmail()==null && user.getEmail()!=null){
                this.setEmail(user.getEmail());
            }
            if(this.getName()==null && user.getName()!=null){
                this.setName(user.getName());
            }
            if(this.getSurname()==null && user.getSurname()!=null){
                this.setSurname(user.getSurname());
            }
            if(this.getPassword()==null && user.getPassword()!=null){
                this.setPassword(user.getPassword());
            }
            if(this.getLocomotion()==null && user.getLocomotion()!=null){
                this.setLocomotion(user.getLocomotion());
            }
            if((this.getAddress()==null || this.getAddress().empty()) 
            		&& user.getAddress()!=null && !user.getAddress().empty()){
                if(this.getAddress()==null){
                    this.setAddress(Address.address());
                }
                this.getAddress().merge(user.getAddress());
            }
        }
        return this;
    }
    
    public void mergeContact(List<String> contacts){
    	if(contacts!=null){
    		for (String contact : contacts) {
				if(!this.contacts.contains(contact)){
					this.contacts.add(contact);
				}
			}
    	}
    }
    
    public User save(){
        WriteResult<User, String> result = collection().save(this);
        this.id = result.getSavedObject().id;
        return this;
    }


    
    public void changePassword(final UsernamePasswordAuthUser authUser,
                               final boolean create) {
        LinkedAccount a = this.getAccountByProvider(authUser.getProvider());
        if (a == null) {
            if (create) {
                a = LinkedAccount.create(authUser);
                this.getLinkedAccounts().add(a);
            } else {
                throw new RuntimeException(
                        "Account not enabled for password usage");
            }
        }
        a.setProviderUserId(authUser.getHashedPassword());
        this.save();
    }

    public LinkedAccount getAccountByProvider(final String providerKey) {
        for (LinkedAccount linkedAccount : getLinkedAccounts()){
            if(linkedAccount.getProviderKey().equals(providerKey)){
                return linkedAccount;
            }
        }
        return null;
    }

    public void resetPassword(final UsernamePasswordAuthUser authUser,
                              final boolean create) {
        // You might want to wrap this into a transaction
        this.changePassword(authUser, create);
        TokenAction.deleteByUser(this, TokenAction.Type.PASSWORD_RESET);
    }

    @JsonIgnore
    public Set<String> getProviders() {
        final Set<String> providerKeys = new HashSet<String>(
                linkedAccounts.size());
        for (final LinkedAccount acc : linkedAccounts) {
            providerKeys.add(acc.getProviderKey());
        }
        return providerKeys;
    }

    //////////////////////////////////////////////
    /////////        STATIC //////////////////////
    //////////////////////////////////////////////
    
    public static User merge(final AuthUser oldUser, final AuthUser newUser) {
        return User.findByAuthUserIdentity(oldUser).merge(
                User.findByAuthUserIdentity(newUser));
    }

    public static User findById(String id){
        return collection().findOneById(id);
    }

    public static Boolean isUserWithEmailExists(String email){
        DBCursor<User> cursor = collection().find(DBQuery.is("email",email));
        return cursor.hasNext();
    }

    public static User getUserwithEmail(String email){
        User user = collection().findOne(DBQuery.is("email",email));
        return user;
    }

    public static User create(final AuthUser authUser) {
    	
    	String email = null;
        if (authUser instanceof EmailIdentity) {
        	final EmailIdentity identity = (EmailIdentity) authUser;
            email = identity.getEmail();
        }    	
        User user = null;
        LinkedAccount linkedAccount = LinkedAccount.create(authUser);
        if(email!=null){
        	user = User.getUserwithEmail(email);
        	if(user!=null && !user.getLinkedAccounts().contains(linkedAccount)){
        		user.getLinkedAccounts().add(linkedAccount);
        	}
        }
        if(user==null){
        	user = User.user()
                    .setRoles(Collections.singletonList(SecurityRole.securityRole().setId((new org.bson.types.ObjectId()).toString())
                            .setRoleName(controllers.Application.USER_ROLE)))
            // user.permissions = new ArrayList<UserPermission>();
            // user.permissions.add(UserPermission.findByValue("printers.edit"));
                    .setEmail(email).setEmailValidated(true).setActive(true).setLastLogin(new Date())
                    .setLinkedAccounts(Collections.singletonList(linkedAccount));
        }

        if (authUser instanceof FirstLastNameIdentity) {
            final FirstLastNameIdentity identity = (FirstLastNameIdentity) authUser;
            final String firstName = identity.getFirstName();
            final String lastName = identity.getLastName();
            if (user.getSurname()!=null && firstName != null) {
                user.setSurname(firstName);
            }
            if (user.getName()!=null && lastName != null) {
                user.setName(lastName);
            }
        }
        user.save();
        return user;
    }

    public static boolean existsByAuthUserIdentity(final AuthUserIdentity identity) {
        if (identity instanceof UsernamePasswordAuthUser) {
            User user = User.findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
            return user!=null;
        } else {
            User user = User.findByAuthUserIdentity(identity);
            return user!=null;
        }
    }

    public static void addLinkedAccount(final AuthUser oldUser,
                                        final AuthUser newUser) {
        final User u = User.findByAuthUserIdentity(oldUser);
        u.getLinkedAccounts().add(LinkedAccount.create(newUser));
        u.save();
    }

    public static void verify(final User unverified) {
        // You might want to wrap this into a transaction
        unverified.emailValidated = true;
        unverified.save();
        TokenAction.deleteByUser(unverified, TokenAction.Type.EMAIL_VERIFICATION);
    }

    public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
        if (identity == null) {
            return null;
        }
        if (identity instanceof UsernamePasswordAuthUser) {
            return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
        } else {
            return collection().findOne(DBQuery.and(DBQuery.is("active", true),
                    DBQuery.is("linkedAccounts.providerUserId", identity.getId()),
                    DBQuery.is("linkedAccounts.providerKey", identity.getProvider())));
        }
    }

    public static User findByUsernamePasswordIdentity(final UsernamePasswordAuthUser identity) {
        User user = getUserwithEmail(identity.getEmail());
        if(user!=null){
            for (LinkedAccount linkedAccount : user.getLinkedAccounts()){
                if(linkedAccount.getProviderKey().equals(identity.getProvider())){
                    return user;
                }
            }
        }
        return null;
    }
    
    @Id
    @ObjectId
    public String getId() {
        return id;
    }
    
    public static User user() {
        return new User();
    }

    @Id
    @ObjectId
    @JsonProperty("id")
    public User setId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }
    @JsonProperty("email")
    public User setEmail(String email) {
        this.email = email;
        return this;
    }
    @JsonProperty("password")
    public String getPassword() {
        return password;
    }
    @JsonProperty("password")
    public User setPassword(String password) {
        this.password = password;
        return this;
    }
    @JsonProperty("name")
    public String getName() {
        return name;
    }
    @JsonProperty("name")
    public User setName(String name) {
        this.name = name;
        return this;
    }
    @JsonProperty("surname")
    public String getSurname() {
        return surname;
    }
    @JsonProperty("surname")
    public User setSurname(String surname) {
        this.surname = surname;
        return this;
    }
    @JsonProperty("address")
    public Address getAddress() {
        return address;
    }
    @JsonProperty("address")
    public User setAddress(Address address) {
        this.address = address;
        return this;
    }
    @JsonProperty("roles")
    public List<SecurityRole> getRoles() {
        return roles;
    }
    @JsonProperty("roles")
    public User setRoles(List<SecurityRole> roles) {
        this.roles = roles;
        return this;
    }
    @JsonProperty("permissions")
    public List<UserPermission> getPermissions() {
        return permissions;
    }
    @JsonProperty("permissions")
    public User setPermissions(List<UserPermission> permissions) {
        this.permissions = permissions;
        return this;
    }
    @JsonProperty("linkedAccounts")
    public List<LinkedAccount> getLinkedAccounts() {
        return linkedAccounts;
    }

    @JsonProperty("linkedAccounts")
    public User setLinkedAccounts(List<LinkedAccount> linkedAccounts) {
        this.linkedAccounts = linkedAccounts;
        return this;
    }
    @JsonProperty("emailValidated")
    public Boolean getEmailValidated() {
        return emailValidated;
    }
    @JsonProperty("emailValidated")
    public User setEmailValidated(Boolean emailValidated) {
        this.emailValidated = emailValidated;
        return this;
    }
    @JsonProperty("active")
    public Boolean getActive() {
        return active;
    }

    @JsonProperty("active")
    public User setActive(Boolean active) {
        this.active = active;
        return this;
    }
    @JsonProperty("lastLogin")
    public Date getLastLogin() {
        return lastLogin;
    }
    @JsonProperty("lastLogin")
    public User setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
        return this;
    }
    @JsonProperty("locomotion")
    public Locomotion getLocomotion() {
		return locomotion;
	}
    @JsonProperty("locomotion")
	public User setLocomotion(Locomotion locomotion) {
		this.locomotion = locomotion;
		return this;
	}
    @JsonProperty("version")
	public String getVersion() {
		return version;
	}
    @JsonProperty("version")
	public User setVersion(String version) {
		this.version = version;
		return this;
	}
    @JsonProperty("contacts")
	public List<String> getContacts() {
		return contacts;
	}
    @JsonProperty("contacts")
	public User setContacts(List<String> contacts) {
		this.contacts = contacts;
		return this;
	}
    
}
