package models;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.persistence.Id;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import play.modules.mongodb.jackson.MongoDB;

@MongoCollection(name="events")
public class Event {
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

    private String id;

    @NotNull
    private String name;

    private String description;

    private Date fromDate;

    private Date toDate;

    @NotNull @Valid
    private Address address;

    private Collection<Subscriber> subscribers = new HashSet<Subscriber>();

    private String creatorRef;

    @JsonIgnore @NotNull @Valid
    private User creator;

    //STATIC
    public static Event read(String id){
        return collection.findOneById(id);
    }

    public static Event insert(JsonNode node){
        Event event = objectMapper.convertValue(node, Event.class);
        return event.save();
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

    public static ObjectMapper objectMapper = new ObjectMapper();

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
        WriteResult<Event, String> result = collection().save(this);
        this.id = result.getSavedId();
        return this;
    }

    private void persistOrLoadCreatorAndCreateRef(){
        if(creator!=null){
            if(creator.id()==null){
                creator.save();
            }
            creatorRef = creator.id();
        }
    }

    private void persistUsersOnSubscribers(){
        for(Subscriber subscriber : getSubscribers()){
            subscriber.saveUser();
        }
    }

    public void update(){
        collection().save(this);
    }
    //////SETTERS//////
    @Id
    @ObjectId
    public String getId() {
        return id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }
    @JsonProperty("setDescription")
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

    public Event addSubscriber(Subscriber subscriber){
        if(!subscribers.contains(subscriber)){
            subscribers.add(subscriber);
        }
        return this;
    }
    public Event addAndMergeSubscriber(Subscriber subscriber){
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
    public Event addAndReplaceSubscriber(Subscriber subscriber){
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

    @JsonIgnore
    public User getCreator(){
        if((this.creator==null || (this.creator!=null && this.creator.isEmpty())) && creatorRef!=null){
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
    @JsonProperty("toDate")
    public Event toDate(Date toDate) {
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


