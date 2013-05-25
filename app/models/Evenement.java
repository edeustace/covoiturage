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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import play.modules.mongodb.jackson.MongoDB;

@MongoCollection(name="events")
public class Evenement {

	public static JacksonDBCollection<Evenement, String> collection = MongoDB.getCollection(Evenement.class, String.class);

    public static ObjectMapper objectMapper = new ObjectMapper();

	@Id
	@ObjectId
    public String id;

    public String name;
    
    public String description;

    public Date fromDate;

    public Date toDate;

    public Address address; 
    
    public DBRef<User, String> creatorRef;

    public Collection<Subscriber> subscribers = new ArrayList<Subscriber>();

    @JsonIgnore
    public User creator;
    
    @JsonProperty("creator")
    public void setCreator(User creator) {
		this.creator = creator;
	}

    @JsonIgnore
    public User getCreator(){
        if(this.creator==null && creatorRef!=null){
    	    this.creator = creatorRef.fetch();
        }
    	return this.creator;
    }
    
    private void persistOrLoadCreatorAndCreateRef(){
    	if(creator!=null){
    		String idRef = null;
    		if(creator.id==null){
    			idRef = creator.insert();
    		}else{
    			idRef = creator.id;
    		}
    		creatorRef = new DBRef<User, String>(idRef, User.class);
        }
    }

    public void addCreatorAsSubscriber(){
        if(creator!=null){
            Subscriber subscriberCreator = new Subscriber();
            subscriberCreator.address = creator.address;
            subscriberCreator.name = creator.name;
            subscriberCreator.surname = creator.surname;
            subscriberCreator.email = creator.email;
            if(creatorRef==null){
                persistOrLoadCreatorAndCreateRef();
            }
            subscriberCreator.userRef = creatorRef;
            this.subscribers.add(subscriberCreator);
        }
    }

    private void createCreatorSubscriber(){

    }

    public String insert(){
        persistOrLoadCreatorAndCreateRef();
        addCreatorAsSubscriber();
    	WriteResult<Evenement, String> result = collection.insert(this);
        this.id = result.getSavedId();
    	return this.id;
    }



    public static Evenement read(String id){
        return collection.findOneById(id);
    }

    public static Evenement insert(JsonNode node){
        Evenement evenement = objectMapper.convertValue(node, Evenement.class);
        String id = evenement.insert();
        return evenement;
    }

    public static Evenement update(String id, JsonNode node) throws IOException {
        Evenement evenement = read(id);
        ObjectReader updater = objectMapper.readerForUpdating(evenement);
        evenement = updater.readValue(node);
        evenement.insert();
        return evenement;
    }
}
