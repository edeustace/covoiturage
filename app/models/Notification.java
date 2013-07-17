package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;

import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;
import play.modules.mongodb.jackson.MongoDB;

@MongoCollection(name="notifications")
public class Notification {

    @Id
    @ObjectId
	public String id;
	
	public String idEvent; 
	
	public String type;
	
	public String from;
	
	public String to; 
	
	public String message;

	public Notification() {
		super();
	}

	public Notification(String idEvent, String from, String to, String type, String message) {
		super();
		this.idEvent = idEvent;
		this.type = type;
		this.from = from;
		this.to = to;
		this.message = message;
	} 
	
    //MAJ
    public Notification save(){
        WriteResult<Notification, String> result = collection().save(this);
        this.id = result.getSavedId();
        return this;
    }
    
    public static void delete(String id){
        collection().removeById(id);
    }
    
    public static List<Notification> listNotifications(String idEvent, String idSub){
    	DBCursor<Notification> cursor = collection().find(DBQuery.and(DBQuery.is("idEvent", idEvent), DBQuery.is("to", idSub)));
    	List<Notification> result = new ArrayList<>();
    	while(cursor.hasNext()){
    		result.add(cursor.next());
    	}
    	return result;
    }
	
    public static JacksonDBCollection<Notification, String> collection = null;

	public static void collection(JacksonDBCollection<Notification, String> collection) {
		Notification.collection = collection;
    }
    public static JacksonDBCollection<Notification, String> collection(){
        if(collection==null){
            collection = MongoDB.getCollection(Notification.class, String.class);
        }
        return collection;
    }
	
}
