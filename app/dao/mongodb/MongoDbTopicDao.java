package dao.mongodb;

import com.mongodb.BasicDBObject;
import dao.TopicDao;
import models.Topic;
import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 10/09/13
 * Time: 21:26
 * To change this template use File | Settings | File Templates.
 */
public class MongoDbTopicDao extends AbstractMongoDao<Topic> implements TopicDao {

    @Override
    public void init() {
        BasicDBObject categorieIdEventCreator = new BasicDBObject();
        categorieIdEventCreator.put("categorie", 1);
        categorieIdEventCreator.put("idEvent", 1);
        categorieIdEventCreator.put("creator", 1);
        getCollection().ensureIndex(categorieIdEventCreator);

        BasicDBObject subscribersIdEvent = new BasicDBObject();
        subscribersIdEvent.put("subscribers", 1);
        subscribersIdEvent.put("idEvent", 1);
        getCollection().ensureIndex(subscribersIdEvent);
    }

    @Override
    protected Class<Topic> getClassOfObject() {
        return Topic.class;
    }

    public List<Topic> findByIdEventAndCategorie(String idEvent, Topic.TopicCategorie categorie){
        return toList(getCollection().find(DBQuery.and(DBQuery.is("categorie", categorie), DBQuery.is("idEvent", idEvent))));
    }

    public Topic findByIdEventCategorieAndCreator(String idEvent, Topic.TopicCategorie categorie, String idCreator){
        return  getCollection().findOne(DBQuery.and(DBQuery.is("categorie", categorie), DBQuery.is("idEvent", idEvent), DBQuery.is("creator", idCreator)));
    }

    public List<Topic> findByIdEventAndIdUser(String idEvent, String idUser){
        return toList(getCollection().find(DBQuery.and(DBQuery.is("subscribers", idUser), DBQuery.is("idEvent", idEvent))));
    }

}
