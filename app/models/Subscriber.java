package models;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import models.enums.Locomotion;
import models.validators.EmailAlreadyUsed;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.validator.constraints.Email;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 25/05/13
 * Time: 17:13
 * To change this template use File | Settings | File Templates.
 */
public class Subscriber {

    ///////////    FIELDS  /////////////////////
    private String id;

    private String userRef;

    @JsonIgnore @EmailAlreadyUsed @NotNull @Valid
    private User user;

    @JsonIgnore
    private String name;
    @JsonIgnore
    private String surname;

    @JsonIgnore @NotNull @Email
    private String email;

    @JsonIgnore @NotNull @Valid
    private Address address;

    @JsonIgnore @NotNull
    private Locomotion locomotion;

    ///////////  CLASS METHODS /////////////////

    public void merge(Subscriber other){
        if(!other.isEmpty()){
            if(other.getName()!=null){
                this.setName(other.getName());
            }
            if(other.getSurname()!=null){
                this.setSurname(other.getSurname());
            }
            if(other.getEmail()!=null){
                this.setEmail(other.getEmail());
            }
            if(other.getAddress()!=null){
                this.setAddress(other.getAddress());
            }
            if(other.getLocomotion()!=null){
                this.setLocomotion(other.getLocomotion());
            }
        }
    }

    @JsonIgnore
    public Boolean isEmpty(){
        return name==null && surname==null &&
                locomotion == null && email==null &&
                (address==null || address.empty()) &&
                (user==null || user.isEmpty()) &&
                userRef==null;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Subscriber){
            if(this.id!=null && ((Subscriber) obj).getId()!=null){
                return this.id.equals(((Subscriber)obj).getId());
            }
            if(this.email==null){
                return false;
            }
            return this.email.equals(((Subscriber)obj).getEmail());
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
        if(this.getUser()!=null && !this.getUser().isEmpty() && this.getUser().getId()==null){
            this.getUser().save();
        } else if(this.getUser()==null || this.getUser().isEmpty()){
            this.setUser(User.user().setAddress(getAddress()).setName(getName()).setSurname(getSurname()).setEmail(getEmail())).getUser().save();
        }
        this.setUserRef(this.getUser().getId());
    }

    //////////////////////////////////////////////
    /////////  GETTERS AND SETTERS ///////////////
    //////////////////////////////////////////////
    @JsonProperty("user")
    public User getUser() {
        if (this.userRef!=null && this.userRef!=null){
            this.user = User.findById(this.userRef);
        }
        return user;
    }

    @JsonProperty("user")
    public Subscriber setUser(User user) {
        this.user = user;
        return this;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }
    @JsonProperty("userRef")
    public String getUserRef() {
        return userRef;
    }
    @JsonProperty("userRef")
    public Subscriber setUserRef(String userRef) {
        this.userRef = userRef;
        return this;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }
    @JsonProperty("name")
    public Subscriber setName(String name) {
        this.name = name;
        return this;
    }
    @JsonProperty("surname")
    public String getSurname() {
        return surname;
    }
    @JsonProperty("surname")
    public Subscriber setSurname(String surname) {
        this.surname = surname;
        return this;
    }
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }
    @JsonProperty("email")
    public Subscriber setEmail(String email) {
        this.email = email;
        return this;
    }
    @JsonProperty("address")
    public Address getAddress() {
        return address;
    }
    @JsonProperty("address")
    public Subscriber setAddress(Address address) {
        this.address = address;
        return this;
    }
    @JsonProperty("locomotion")
    public Locomotion getLocomotion() {
        return locomotion;
    }
    @JsonProperty("locomotion")
    public Subscriber setLocomotion(Locomotion locomotion) {
        this.locomotion = locomotion;
        return this;
    }
}
