package models;

import net.vz.mongodb.jackson.*;
import play.modules.mongodb.jackson.MongoDB;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 25/07/13
 * Time: 20:26
 * To change this template use File | Settings | File Templates.
 */

@MongoCollection(name="messages")
public class ChatMessage {

    @Id
    @ObjectId
    public String id;

    public String type = "message";

    public Date date;

    @NotNull
    public String from;

    public String topicRef;

    @NotNull
    public Topic topic;

    @NotNull
    public String message;


    public ChatMessage() {
        this.date = new Date();
    }

    ////////////  STATIC  ////////////////
    public ChatMessage save(){
        if(this.topicRef==null){
            this.topicRef = topic.id;
        }
        WriteResult<ChatMessage, String> result = collection().save(this);
        this.id = result.getSavedId();
        return this;
    }

    public static List<ChatMessage> findByIdTopic(String idTopic){
        DBCursor<ChatMessage> cursor = collection().find(DBQuery.is("topicRef", idTopic));
        List<ChatMessage> result = new ArrayList<ChatMessage>();
        while(cursor.hasNext()){
            result.add(cursor.next());
        }
        return result;
    }

    public static JacksonDBCollection<ChatMessage, String> collection = null;

    public static void collection(JacksonDBCollection<ChatMessage, String> collection) {
        ChatMessage.collection = collection;
    }
    public static JacksonDBCollection<ChatMessage, String> collection(){
        if(collection==null){
            collection = MongoDB.getCollection(ChatMessage.class, String.class);
        }
        return collection;
    }

}
