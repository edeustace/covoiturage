package models;

import dao.TopicDao;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;

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
public class Topic extends AbstractModel {

    public static enum TopicCategorie {
        carChat, chat, wall
    }


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

    private static TopicDao dao;

    private static TopicDao getDao(){
        return Topic.dao;
    }

    public static void setDao(TopicDao dao){
        Topic.dao = dao;
    }



    public Topic save(){
        return getDao().save(this);
    }

    public Topic update(){
        return getDao().update(this);
    }


    public static Topic getById(String id){
        return getDao().get(id);
    }

    public static Topic exists(Topic topic){

        if(topic.getId() != null){
            Topic db = getById(topic.getId());
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
            if(topic.getId() != null && this.getId()!=null && topic.getId().equals(this.getId())){
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
        return getDao().findByIdEventAndCategorie(idEvent, categorie);
    }

    public static Topic findByIdEventCategorieAndCreator(String idEvent, TopicCategorie categorie, String idCreator){
        return getDao().findByIdEventCategorieAndCreator(idEvent, categorie, idCreator);
    }

    public static List<Topic> findByIdEventAndIdUser(String idEvent, String idUser){
        return getDao().findByIdEventAndIdUser(idEvent, idUser);
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
