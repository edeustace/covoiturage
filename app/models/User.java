package models;

import javax.persistence.Id;

import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;
import play.modules.mongodb.jackson.MongoDB;

@MongoCollection(name="users")
public class User {
	
	public static JacksonDBCollection<User, String> collection = MongoDB.getCollection(User.class, String.class);
	
	@Id
	@ObjectId
	public String id; 
	
	public String email; 
	
	public String password; 
	
	public String name; 
	
	public String surname; 
	
	public Address address; 
	
	public String insert(){
    	WriteResult<User, String> result = collection.insert(this);
    	return result.getSavedObject().id;
    }
	
}
