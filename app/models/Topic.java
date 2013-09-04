package models;

import net.vz.mongodb.jackson.*;
import org.springframework.util.CollectionUtils;
import play.data.validation.Constraints;
import play.modules.mongodb.jackson.MongoDB;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
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

    public static enum TopicCategorie {
        carChat, chat, wall
    }

    @Id
    @ObjectId

    public String id;

    @NotNull
    public String idEvent;

    public String type = "topic";

    public TopicCategorie categorie;

    public String tmpId;

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

    public Topic update(){
        WriteResult<Topic, String> result = collection().updateById(this.id, this);
        return this;
    }


    public static Topic getById(String id){
        return collection().findOneById(id);
    }

    public static Topic exists(Topic topic){

        if(topic.id != null){
            Topic db = collection().findOneById(topic.id);
            if(db!=null)
                return db;
        }
        List<Topic> topics = findByIdEventAndCategorie(topic.idEvent, topic.categorie);
        if(!topics.isEmpty()){
            for(Topic aTopic : topics){
                if(topic.equals(aTopic)){
                    return aTopic;
                }
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof Topic){
            Topic topic = (Topic)obj;
            if(topic.id != null && this.id!=null && topic.id.equals(this.id)){
                return true;
            }
            if(topic.categorie != null && this.categorie!=null && topic.categorie.equals(this.categorie) &&
                topic.idEvent != null && this.idEvent!=null && topic.idEvent.equals(this.idEvent)){

                if((topic.subscribers==null || topic.subscribers.isEmpty()) && (this.subscribers==null || this.subscribers.isEmpty())){
                    return true;
                }
                if(topic.subscribers!=null && !topic.subscribers.isEmpty() && this.subscribers!=null && !this.subscribers.isEmpty()){
                    List<String> current = new ArrayList<String>();
                    current.addAll(this.subscribers);
                    List<String> other = new ArrayList<String>();
                    other.addAll(topic.subscribers);

                    current.removeAll(other);
                    if(!current.isEmpty()){
                        return false;
                    }
                    current = new ArrayList<String>();
                    current.addAll(this.subscribers);

                    other.removeAll(current);
                    if(other.isEmpty()){
                        return true;
                    }
                }
            }
            return false;
        }

        return super.equals(obj);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public static boolean subscribersEquals(List<String> subscribers1, List<String> subscribers2){
        if(subscribers2!=null && !subscribers2.isEmpty() && subscribers1!=null && !subscribers1.isEmpty()){
            List<String> current = new ArrayList<String>();
            current.addAll(subscribers1);
            List<String> other = new ArrayList<String>();
            other.addAll(subscribers2);

            current.removeAll(other);
            if(!current.isEmpty()){
                return false;
            }
            current = new ArrayList<String>();
            current.addAll(subscribers1);

            other.removeAll(current);
            if(other.isEmpty()){
                return true;
            }
        }
        return false;
    }

    public static List<Topic> findByIdEventAndCategorie(String idEvent, TopicCategorie categorie){
        DBCursor<Topic> cursor = collection().find(DBQuery.and(DBQuery.is("categorie", categorie), DBQuery.is("idEvent", idEvent)));
        List<Topic> result = new ArrayList<Topic>();
        while(cursor.hasNext()){
            result.add(cursor.next());
        }
        return result;
    }

    public static Topic findByIdEventCategorieAndCreator(String idEvent, TopicCategorie categorie, String idCreator){
        return  collection().findOne(DBQuery.and(DBQuery.is("categorie", categorie), DBQuery.is("idEvent", idEvent), DBQuery.is("creator", idCreator)));
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



    public String getId() {
        return id;
    }

    public String getIdEvent() {
        return idEvent;
    }

    public String getType() {
        return type;
    }

    public TopicCategorie getCategorie() {
        return categorie;
    }

    public String getTmpId() {
        return tmpId;
    }

    public Date getDate() {
        return date;
    }

    public Date getUpdate() {
        return update;
    }

    public String getCreator() {
        return creator;
    }

    public List<String> getSubscribers() {
        return subscribers;
    }
}
