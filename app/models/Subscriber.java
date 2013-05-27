package models;

import models.enums.Locomotion;
import net.vz.mongodb.jackson.DBRef;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 25/05/13
 * Time: 17:13
 * To change this template use File | Settings | File Templates.
 */
public class Subscriber {

    private DBRef<User, String> userRef;

    @JsonIgnore
    private User user;

    private String name;

    private String surname;

    private String email;

    private Address address;

    private Locomotion locomotion;

    public static Subscriber subscriber(){
        return new Subscriber();
    }


    public User user() {
        if(this.user!=null && !user.empty()){
            return user;
        }else if (this.userRef!=null && this.userRef.getId()!=null){
            this.user = userRef.fetch();
        }else{
            this.user = User.user();
        }
        return user;
    }

    public Subscriber user(User user) {
        this.user = user;
        return this;
    }

    @JsonProperty("userRef")
    public DBRef<User, String> userRef() {
        return userRef;
    }
    @JsonProperty("userRef")
    public Subscriber userRef(DBRef<User, String> userRef) {
        this.userRef = userRef;
        return this;
    }

    @JsonProperty("name")
    public String name() {
        return name;
    }
    @JsonProperty("name")
    public Subscriber name(String name) {
        this.name = name;
        return this;
    }
    @JsonProperty("surname")
    public String surname() {
        return surname;
    }
    @JsonProperty("surname")
    public Subscriber surname(String surname) {
        this.surname = surname;
        return this;
    }
    @JsonProperty("email")
    public String email() {
        return email;
    }
    @JsonProperty("email")
    public Subscriber email(String email) {
        this.email = email;
        return this;
    }
    @JsonProperty("address")
    public Address address() {
        return address;
    }
    @JsonProperty("address")
    public Subscriber address(Address address) {
        this.address = address;
        return this;
    }
    @JsonProperty("locomotion")
    public Locomotion locomotion() {
        return locomotion;
    }
    @JsonProperty("locomotion")
    public Subscriber locomotion(Locomotion locomotion) {
        this.locomotion = locomotion;
        return this;
    }
}
