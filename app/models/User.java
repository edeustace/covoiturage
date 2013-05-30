package models;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.validator.constraints.Email;
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

    @NotNull @Email
    private String email;

    @NotNull
    private String password;

    private String name;

    private String surname;

    private Address address;

    public Boolean isEmpty(){
        return id==null && email == null &&
                password==null && name==null &&
                surname == null && address==null;
    }

    public void merge(User user){
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

    public User save(){
    	WriteResult<User, String> result = collection().save(this);
        this.id = result.getSavedObject().id;
    	return this;
    }

    public static User findById(String id){
        return collection().findOneById(id);
    }
}
