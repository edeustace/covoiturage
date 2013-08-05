package models;

import net.vz.mongodb.jackson.*;
import play.modules.mongodb.jackson.MongoDB;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@MongoCollection(name="notifications")
public class Notification {

    @Id
    @ObjectId
	public String id;
	
	public String idEvent; 
	
	public String type = "notification";

    public String categorie;
	
	public String from;
	
	public String to; 
	
	public String message;

    public Date date;

	public Notification() {
		super();
	}

	public Notification(String idEvent, String from, String to, String categorie, String message, Date date) {
		super();
		this.idEvent = idEvent;
		this.categorie = categorie;
		this.from = from;
		this.to = to;
		this.message = message;
        this.date = date;
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
    	List<Notification> result = new ArrayList<Notification>();
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
