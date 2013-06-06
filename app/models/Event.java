package models;

import java.io.IOException;
import java.util.*;

import javax.persistence.Id;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import models.validators.EmailAlreadyUsed;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import play.data.validation.Validation;
import play.modules.mongodb.jackson.MongoDB;

@MongoCollection(name="events")
public class Event {

    ///////////    FIELDS  /////////////////////
    private String id;

    @NotNull
    private String name;

    private String description;

    private Date fromDate;

    private Date toDate;

    @NotNull @Valid
    private Address address;

    @Valid @NotNull
    private Collection<Subscriber> subscribers = new ArrayList<Subscriber>();

    private String creatorRef;

    @JsonIgnore @NotNull @EmailAlreadyUsed @Valid
    private User creator;

    ///////////  CLASS METHODS /////////////////

    public void merge(Event event){
        if(event.getName()==null){
            this.setName(event.getName());
        }
        if(event.getDescription()==null){
            this.setDescription(event.getDescription());
        }
        if(event.getFromDate()==null){
            this.setFromDate(event.getFromDate());
        }
        if(event.getToDate()==null){
            this.setToDate(event.getToDate());
        }
        if(event.getAddress()!=null && !event.getAddress().empty()){
            if(this.getAddress()==null){
                this.setAddress(Address.address());
            }
            this.getAddress().merge(event.getAddress());
        }
        if(event.getCreatorRef()!=null){
            this.setCreatorRef(event.getCreatorRef());
        }
        if(event.getSubscribers()!=null && !event.getSubscribers().isEmpty()){
            for(Subscriber subscriber : event.getSubscribers()){
                this.addAndMergeSubscriber(subscriber);
            }
        }
    }

    @JsonIgnore
    public Boolean isEmpty(){
        return this.name==null && this.description==null &&
                this.fromDate==null && this.toDate==null &&
                this.creatorRef==null &&
                (this.address==null || this.address.empty()) &&
                (this.subscribers==null || this.subscribers.isEmpty()) &&
                (this.creator==null || this.creator.isEmpty());
    }

    //MAJ
    public Event save(){
        this.persistOrLoadCreatorAndCreateRef();
        this.addCreatorAsSubscriber();
        this.persistUsersOnSubscribers();
        this.setFromDate(new Date());
        WriteResult<Event, String> result = collection().save(this);
        this.id = result.getSavedId();
        return this;
    }

    private void persistOrLoadCreatorAndCreateRef(){
        if(creator!=null){
            if(creator.getId()==null){
                creator.save();
            }
            creatorRef = creator.getId();
        }
    }

    private void persistUsersOnSubscribers(){
        int maxId = 0;
        for(Subscriber subscriber : getSubscribers()){
            subscriber.saveUser();
            if(subscriber.getId()!=null){
                Integer id = Integer.valueOf(subscriber.getId());
                if(id>maxId){
                    maxId = id;
                }
            }
        }
        for(Subscriber subscriber : getSubscribers()){
            if(subscriber.getId()==null){
                subscriber.setId(String.valueOf(maxId));
                maxId++;
            }
        }
    }


    //////////////////////////////////////////////
    /////////        STATIC //////////////////////
    //////////////////////////////////////////////
    public static ObjectMapper objectMapper = new ObjectMapper();
    public static JacksonDBCollection<Event, String> collection = null;

    public static void collection(JacksonDBCollection<Event, String> collection) {
        Event.collection = collection;
    }
    public static JacksonDBCollection<Event, String> collection(){
        if(collection==null){
            collection = MongoDB.getCollection(Event.class, String.class);
        }
        return collection;
    }

    public static Event read(String id){
        return collection().findOneById(id);
    }

    public static Event event(){
        return new Event();
    }

    public static Event update(String id, JsonNode node) throws IOException {
        Event event = read(id);
        ObjectReader updater = objectMapper.readerForUpdating(event);
        event = updater.readValue(node);
        event.save();
        return event;
    }


    //////////////////////////////////////////////
    /////////  GETTERS AND SETTERS ///////////////
    //////////////////////////////////////////////
    @Id
    @ObjectId
    public String getId() {
        return id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }
    @JsonProperty("fromDate")
    public Date getFromDate() {
        return fromDate;
    }
    @JsonProperty("toDate")
    public Date getToDate() {
        return toDate;
    }
    @JsonProperty("address")
    public Address getAddress() {
        return address;
    }
    @JsonProperty("subscribers")
    public Collection<Subscriber> getSubscribers() {
        return subscribers;
    }

    public Event addSubscriber(@Valid Subscriber subscriber){
        if(!subscribers.contains(subscriber)){
            subscribers.add(subscriber);
        }
        return this;
    }
    public Event addAndMergeSubscriber(@Valid Subscriber subscriber){
        if(!subscribers.contains(subscriber)){
            subscribers.add(subscriber);
        }else{
            for(Subscriber aSubscriber : subscribers){
                if(aSubscriber.equals(subscriber)){
                    //Merge
                    aSubscriber.merge(subscriber);
                }
            }
        }
        return this;
    }

    public Subscriber getSubscriberByMail(String email){
        for(Subscriber subscriber : subscribers){
            if(subscriber.getEmail()!=null && subscriber.getEmail().equals(email)){
                return subscriber;
            }
        }
        return null;
    }

    public Subscriber getSubscriberById(String id){
        for(Subscriber subscriber : subscribers){
            if(subscriber.getId()!=null && subscriber.getId().equals(id)){
                return subscriber;
            }
        }
        return null;
    }

    public Event addAndReplaceSubscriber(@Valid Subscriber subscriber){
        if(!subscribers.contains(subscriber)){
            subscribers.add(subscriber);
        }else{
            for(Subscriber aSubscriber : subscribers){
                if(aSubscriber.equals(subscriber)){
                    subscribers.remove(aSubscriber);
                    break;
                }
            }
            subscribers.add(subscriber);
        }
        return this;
    }

    private void addCreatorAsSubscriber(){
        if(creator!=null){
            Subscriber subscriberCreator = Subscriber
                    .subscriber().setAddress(creator.getAddress())
                    .setName(creator.getName()).setSurname(creator.getSurname())
                    .setEmail(creator.getEmail()).setUser(creator);
            this.subscribers.add(subscriberCreator);
        }
    }

    @JsonProperty("creator")
    public User getCreator(){
        if(creatorRef!=null){
            this.creator = User.findById(creatorRef);
        }
        return this.creator;
    }

    @JsonProperty("creatorRef")
    public String getCreatorRef() {
        return creatorRef;
    }

    @JsonProperty("creator")
    public Event setCreator(User creator) {
        this.creator = creator;
        return this;
    }

    @Id
    @ObjectId
    public Event setId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("name")
    public Event setName(String name) {
        this.name = name;
        return this;
    }
    @JsonProperty("description")
    public Event setDescription(String description) {
        this.description = description;
        return this;
    }
    @JsonProperty("fromDate")
    public Event setFromDate(Date fromDate) {
        this.fromDate = fromDate;
        return this;
    }
    @JsonProperty("toDate")
    public Event setToDate(Date toDate) {
        this.toDate = toDate;
        return this;
    }
    @JsonProperty("address")
    public Event setAddress(Address address) {
        this.address = address;
        return this;
    }
    @JsonProperty("creatorRef")
    public Event setCreatorRef(String creatorRef) {
        this.creatorRef = creatorRef;
        return this;
    }
}


