package models;

import javax.persistence.Id;

import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;
import org.codehaus.jackson.annotate.JsonProperty;
import play.modules.mongodb.jackson.MongoDB;

@MongoCollection(name="users")
public class User {
	
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

	private String id;

    private String email;

    private String password;

    private String name;

    private String surname;

    private Address address;

    public Boolean empty(){
        return id==null && email == null &&
                password==null && name==null &&
                surname == null && address==null;
    }

    @Id
    @ObjectId
    public String id() {
        return id;
    }

    public static User user() {
        return new User();
    }

    @Id
    @ObjectId
    public User id(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("email")
    public String email() {
        return email;
    }
    @JsonProperty("email")
    public User email(String email) {
        this.email = email;
        return this;
    }
    @JsonProperty("password")
    public String password() {
        return password;
    }
    @JsonProperty("password")
    public User password(String password) {
        this.password = password;
        return this;
    }
    @JsonProperty("name")
    public String name() {
        return name;
    }
    @JsonProperty("name")
    public User name(String name) {
        this.name = name;
        return this;
    }
    @JsonProperty("surname")
    public String surname() {
        return surname;
    }
    @JsonProperty("surname")
    public User surname(String surname) {
        this.surname = surname;
        return this;
    }
    @JsonProperty("address")
    public Address address() {
        return address;
    }
    @JsonProperty("address")
    public User address(Address address) {
        this.address = address;
        return this;
    }

    public User insert(){
    	WriteResult<User, String> result = collection().insert(this);
        this.id = result.getSavedObject().id;
    	return this;
    }
	
}
