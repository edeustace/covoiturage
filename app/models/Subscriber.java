package models;

import models.enums.Locomotion;
import net.vz.mongodb.jackson.DBRef;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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

    public void merge(Subscriber other){
        if(!other.empty()){
            if(other.name()!=null){
                this.name(other.name());
            }
            if(other.surname()!=null){
                this.surname(other.surname());
            }
            if(other.email()!=null){
                this.email(other.email());
            }
            if(other.address()!=null){
                this.address(other.address());
            }
            if(other.locomotion()!=null){
                this.locomotion(other.locomotion());
            }
        }
    }

    public Boolean empty(){
        return name==null && surname==null &&
                locomotion == null && email==null &&
                (address==null || address.empty()) &&
                (user==null || user.empty()) &&
                userRef==null;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Subscriber){
            if(this.email==null){
                return false;
            }
            return this.email.equals(((Subscriber)obj).email());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (new HashCodeBuilder()).append(email).hashCode();
    }

    public static Subscriber subscriber(){
        return new Subscriber();
    }

    public void saveUser(){
        if(!this.user().empty() && this.user().id()==null){
            this.user().insert();
        } else if(this.user().empty()){
            this.user(User.user().address(address()).name(name()).surname(surname()).email(email())).user().insert();
        }
        this.userRef(new DBRef<User, String>(this.user().id(), User.class));
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
        if(address==null){
            address = Address.address();
        }
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
