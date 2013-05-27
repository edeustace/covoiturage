package models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Id;

import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import play.modules.mongodb.jackson.MongoDB;

@MongoCollection(name="events")
public class Event {

    private String id;

    private String name;

    private String description;

    private Date fromDate;

    private Date toDate;

    public Address address;

    private Collection<Subscriber> subscribers = new ArrayList<Subscriber>();

    private DBRef<User, String> creatorRef;

    @JsonIgnore
    private User creator;

    //STATIC
    public static Event read(String id){
        return collection.findOneById(id);
    }

    public static Event insert(JsonNode node){
        Event event = objectMapper.convertValue(node, Event.class);
        return event.insert();
    }

    public static Event newOne(){
        return new Event();
    }

    public static Event update(String id, JsonNode node) throws IOException {
        Event event = read(id);
        ObjectReader updater = objectMapper.readerForUpdating(event);
        event = updater.readValue(node);
        event.insert();
        return event;
    }

    public static JacksonDBCollection<Event, String> collection = MongoDB.getCollection(Event.class, String.class);
    public static void collection(JacksonDBCollection<Event, String> collection) {
        Event.collection = collection;
    }
    public static ObjectMapper objectMapper = new ObjectMapper();

    //MAJ
    public Event insert(){
        persistOrLoadCreatorAndCreateRef();
        addCreatorAsSubscriber();
        WriteResult<Event, String> result = collection.insert(this);
        this.id = result.getSavedId();
        return this;
    }
    private void persistOrLoadCreatorAndCreateRef(){
        if(creator!=null){
            String idRef;
            if(creator.id()==null){
                idRef = creator.insert().id();
            }else{
                idRef = creator.id();
            }
            creatorRef = new DBRef<User, String>(idRef, User.class);
        }
    }

    public void update(){
        collection.save(this);
    }
    //////SETTERS//////
    @Id
    @ObjectId
    public String id() {
        return id;
    }

    @JsonProperty("name")
    public String name() {
        return name;
    }
    @JsonProperty("description")
    public String description() {
        return description;
    }
    @JsonProperty("fromDate")
    public Date fromDate() {
        return fromDate;
    }
    @JsonProperty("toDate")
    public Date toDate() {
        return toDate;
    }
    @JsonProperty("address")
    public Address address() {
        return address;
    }
    @JsonProperty("subscribers")
    public Collection<Subscriber> subscribers() {
        return subscribers;
    }

    public Event addSubscriber(Subscriber subscriber){
        if((subscriber.userRef()==null || StringUtils.isEmpty(subscriber.userRef().getId()))
                && !subscriber.user().empty()){
            String id = null;
            User user = subscriber.user();
            if(user.id()!=null){
                id = user.id();
            }else{
                id = user.insert().id();
            }
            subscriber.userRef(new DBRef<User, String>(id, User.class));
        }
        subscribers.add(subscriber);
        return this;
    }

    private void addCreatorAsSubscriber(){
        if(creator!=null){
            Subscriber subscriberCreator = Subscriber
                    .subscriber().address(creator.address())
                    .name(creator.name()).surname(creator.surname())
                    .email(creator.email());
            if(creatorRef()==null){
                persistOrLoadCreatorAndCreateRef();
            }
            subscriberCreator.userRef(creatorRef);
            this.subscribers.add(subscriberCreator);
        }
    }

    @JsonIgnore
    public User creator(){
        if((this.creator==null || (this.creator!=null && this.creator.empty())) && creatorRef!=null){
            this.creator = creatorRef.fetch();
        }else if(this.creator==null && creatorRef==null){
            this.creator = User.user();
        }
        return this.creator;
    }

    @JsonProperty("creatorRef")
    public DBRef<User, String> creatorRef() {
        return creatorRef;
    }

    @JsonProperty("creator")
    public Event creator(User creator) {
        this.creator = creator;
        return this;
    }

    @Id
    @ObjectId
    public Event id(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("name")
    public Event name(String name) {
        this.name = name;
        return this;
    }
    @JsonProperty("description")
    public Event description(String description) {
        this.description = description;
        return this;
    }
    @JsonProperty("toDate")
    public Event toDate(Date toDate) {
        this.toDate = toDate;
        return this;
    }
    @JsonProperty("address")
    public Event address(Address address) {
        this.address = address;
        return this;
    }
    @JsonProperty("creatorRef")
    public Event creatorRef(DBRef<User, String> creatorRef) {
        this.creatorRef = creatorRef;
        return this;
    }



}


