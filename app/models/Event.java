package models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Id;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dao.EventDao;
import dao.RepositoryLocator;
import models.validators.EmailAlreadyUsed;
import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBQuery;
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

@JsonIgnoreProperties(ignoreUnknown = true)
@MongoCollection(name="events")
public class Event extends AbstractModel {

	public static @interface Update{}
	public static @interface Create{}
	
    ///////////    FIELDS  /////////////////////
    private String version = "1";

	@NotNull(message = "event.name.notNull")
    private String name;

    private String description;

    private Date fromDate;

    private Date toDate;

    private Integer hour;
    private Integer minutes;

    private List<String> contacts = new ArrayList<String>();
    
    private Boolean contactsOnly = Boolean.FALSE;

    private Boolean updated = Boolean.FALSE;

    @NotNull(message = "event.address.notNull") @Valid
    private Address address;

    @Valid @NotNull
    private Collection<Subscriber> subscribers = new ArrayList<Subscriber>();

    private String creatorRef;

    @JsonIgnore @Valid @EmailAlreadyUsed
    private User creator;

    ///////////  CLASS METHODS /////////////////

    public void merge(Event event){
        if(event.getName()!=null){
            this.setName(event.getName());
        }
        if(event.getDescription()!=null){
            this.setDescription(event.getDescription());
        }
        if(event.getUpdated()!=null){
            this.setUpdated(event.getUpdated());
        }
        if(event.getFromDate()!=null){
            this.setFromDate(event.getFromDate());
        }
        if(event.getToDate()!=null){
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
        if(event.getContactsOnly()!=null){
            this.setContactsOnly(event.getContactsOnly());
        }
        if(event.getHour()!=null) {
            this.setHour(event.getHour());
        }
        if(event.getMinutes()!=null){
            this.setMinutes(event.getMinutes());
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
        if(this.getFromDate()==null){
            this.setFromDate(new Date());
        }
        this.setCreator(null);
        return getDao().save(this);
    }
    
    public Event update(){
        return getDao().save(this);
    }

    public Event addPassenger(String idPassenger, String idCarOwner){
    	if(StringUtils.isNotBlank(idPassenger)){
    		for (Subscriber subscriber : this.getSubscribers()) {
    			if(subscriber.getUserRef().equals(idCarOwner)){
    	    		if(subscriber.getCar()!=null && !subscriber.getCar().getPassengers().contains(idPassenger)){
    	    			subscriber.getCar().addPassenger(idPassenger);
    	        	}
    			}else {
    				if(subscriber.getCar()!=null && subscriber.getCar().getPassengers().contains(idPassenger)){
    					subscriber.getCar().getPassengers().remove(idPassenger);
    				}
					if(subscriber.getCar()!=null && subscriber.getCar().getWaitingList().contains(idPassenger)){
						subscriber.getCar().getWaitingList().remove(idPassenger);
					}
				}
			}
        }
    	Subscriber passenger = this.getSubscriberById(idPassenger);
    	if(passenger!=null){
    		passenger.setCarRef(idCarOwner);
    		passenger.getPossibleCars().remove(idCarOwner);
    	}
    	return this;
    }

    public Event removeCar(String idCarOwner){
        Subscriber carOwner = this.getSubscriberById(idCarOwner);
        if(carOwner.getCar()!=null && carOwner.getCar().getPassengers()!=null){
            for(String pass : carOwner.getCar().getPassengers()){
                Subscriber passenger = this.getSubscriberById(pass);
                if(passenger!=null && passenger.getCarRef()!=null && passenger.getCarRef().equals(idCarOwner)){
                    passenger.setCarRef(null);
                }
            }
            carOwner.setCar(null);
        }
        for (Subscriber subscriber : this.getSubscribers()) {
            if(!subscriber.getUserRef().equals(idCarOwner)){
                if(subscriber.getPossibleCars()!=null && subscriber.getPossibleCars().contains(idCarOwner)){
                    subscriber.getPossibleCars().remove(idCarOwner);
                }
            }
        }
        return this;
    }

    public Event deletePassenger(String idPassenger, String idCarOwner){
    	Subscriber carOwner = this.getSubscriberById(idCarOwner);
    	if(carOwner.getCar()!=null && carOwner.getCar().getPassengers()!=null){
    		carOwner.getCar().getPassengers().remove(idPassenger);	
    	}
    	Subscriber passenger = this.getSubscriberById(idPassenger);
    	if(passenger!=null && passenger.getCarRef()!=null && passenger.getCarRef().equals(idCarOwner)){
    		passenger.setCarRef(null);
    	}
    	return this;
    }
    
    private void persistOrLoadCreatorAndCreateRef(){
    	if(creatorRef!=null){
    		User user = User.findById(creatorRef);
    		user.mergeIfNull(creator);
    		user.mergeContact(this.getContacts());
    		user.save();
    		if(creator!=null && !creator.isEmpty()){
    			creator = user;	
    		}
    	}else if(creator!=null){
            if(creator.getId()==null){
            	creator.setLastLogin(new Date());
            	creator.save();
            }
            creatorRef = creator.getId();
        }
    }

    public List<String> addContactsAndSave(List<String> contactsToAdd){
    	List<String> addedcontacts = new ArrayList<String>();
    	for (String string : contactsToAdd) {
			if(!this.getContacts().contains(string)){
				this.getContacts().add(string);
				addedcontacts.add(string);
			}
		}
    	if(creatorRef!=null){
    		User user = User.findById(creatorRef);
    		user.mergeContact(this.getContacts());
    		user.save();
    	}
    	this.save();
    	return addedcontacts;
    }
    
    private void persistUsersOnSubscribers(){
        for(Subscriber subscriber : getSubscribers()){
            subscriber.saveUser();
            subscriber.setUser(null);
        }
    }

    public User loadCreator(){
    	if(this.creatorRef!=null){
    		return User.findById(creatorRef);
    	}
    	return null;
    }

    //////////////////////////////////////////////
    /////////        STATIC //////////////////////
    //////////////////////////////////////////////

    private static EventDao getDao(){
        return RepositoryLocator.getRepositoryLocator().getEventDao();
    }

    public static ObjectMapper objectMapper = new ObjectMapper();


    public static List<Event> listByUser(String idUser){
    	return getDao().listByUser(idUser);
    }
    
    public static Event read(String id){
        return getDao().get(id);
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

    public static List<Event> listInvitedByEmail(String email){
        return getDao().listInvitedByEmail(email);
    }

    //////////////////////////////////////////////
    /////////  GETTERS AND SETTERS ///////////////
    //////////////////////////////////////////////

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

    public Boolean containsSubscriber(Subscriber subscriber){
        return subscribers.contains(subscriber);
    }
    
    public String getIdSubscriber(Subscriber aSubscriber){
    	for (Subscriber subscriber : this.getSubscribers()) {
			if(subscriber.equals(aSubscriber)){
				return subscriber.getUserRef();
			}
		}
        return null;
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
            if(subscriber.getUserRef()!=null && subscriber.getUserRef().equals(id)){
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
        if(creator!=null && !creator.isEmpty() && subscribers!=null && !subscribers.contains(creator)){
            Subscriber subscriberCreator = Subscriber
                    .subscriber().setAddress(creator.getAddress())
                    .setName(creator.getName()).setSurname(creator.getSurname())
                    .setEmail(creator.getEmail()).setUser(creator).setLocomotion(creator.getLocomotion());
            this.subscribers.add(subscriberCreator);
        }
    }

    @JsonProperty("creator")
    public User getCreator(){
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
    @JsonProperty("version")
    public String getVersion() {
		return version;
	}
    @JsonProperty("version")
	public Event setVersion(String version) {
		this.version = version;
		return this;
	}
    @JsonProperty("contacts")
    public List<String> getContacts() {
		return contacts;
	}
    @JsonProperty("contacts")
	public Event setContacts(List<String> contacts) {
		this.contacts = contacts;
		return this;
	}
    @JsonProperty("contactsOnly")
	public Boolean getContactsOnly() {
		return contactsOnly;
	}
    @JsonProperty("contactsOnly")
	public Event setContactsOnly(Boolean contactsOnly) {
		this.contactsOnly = contactsOnly;
		return this;
    }
    @JsonProperty("updated")
    public Boolean getUpdated() {
        return updated;
    }
    @JsonProperty("updated")
    public Event setUpdated(Boolean updated) {
        this.updated = updated;
        return this;
    }
    @JsonProperty("hour")
    public Integer getHour() {
        return hour;
    }
    @JsonProperty("hour")
    public Event setHour(Integer hour) {
        this.hour = hour;
        return this;
    }
    @JsonProperty("minutes")
    public Integer getMinutes() {
        return minutes;
    }
    @JsonProperty("minutes")
    public Event setMinutes(Integer minutes) {
        this.minutes = minutes;
        return this;
    }
}