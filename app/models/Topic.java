package models;

import net.vz.mongodb.jackson.*;
import play.data.validation.Constraints;
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
 * Time: 20:24
 * To change this template use File | Settings | File Templates.
 */


@MongoCollection(name="topics")
public class Topic {

    @Id
    @ObjectId
    public String id;

    @NotNull
    public String idEvent;

    public String type = "topic";

    public Date date;

    public Date update;

    @NotNull
    public String creator;

    public List<String> subscribers = new ArrayList<String>();

    public Topic() {
        super();
        this.date = new Date();
        this.update = this.date;
    }

    ////////////  STATIC  ////////////////
    public Topic save(){
        WriteResult<Topic, String> result = collection().save(this);
        this.id = result.getSavedId();
        return this;
    }

    public static Topic getById(String id){
        return collection().findOneById(id);
    }

    public static List<Topic> findByIdEventAndIdUser(String idEvent, String idUser){
        DBCursor<Topic> cursor = collection().find(DBQuery.and(DBQuery.is("subscribers", idUser), DBQuery.is("idEvent", idEvent)));
        List<Topic> result = new ArrayList<Topic>();
        while(cursor.hasNext()){
            result.add(cursor.next());
        }
        return result;
    }

    public static JacksonDBCollection<Topic, String> collection = null;

    public static void collection(JacksonDBCollection<Topic, String> collection) {
        Topic.collection = collection;
    }
    public static JacksonDBCollection<Topic, String> collection(){
        if(collection==null){
            collection = MongoDB.getCollection(Topic.class, String.class);
        }
        return collection;
    }
}
