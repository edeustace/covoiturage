package dao.mongodb;

import com.mongodb.BasicDBObject;
import dao.EventDao;
import models.Event;
import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 10/09/13
 * Time: 21:08
 * To change this template use File | Settings | File Templates.
 */
public class MongoDbEventDao extends AbstractMongoDao<Event> implements EventDao {
    @Override
    protected Class<Event> getClassOfObject() {
        return Event.class;
    }

    @Override
    public List<Event> listByUser(String idUser){
        return toList(getCollection().find(DBQuery.or(DBQuery.is("subscribers.userRef", idUser), DBQuery.is("creatorRef", idUser))));
    }

    @Override
    public List<Event> listInvitedByEmail(String email) {
        return toList(getCollection().find(DBQuery.is("contacts", email)));
    }


    @Override
    public void init() {
        getCollection().ensureIndex(new BasicDBObject("subscribers.userRef", 1));
        getCollection().ensureIndex(new BasicDBObject("creatorRef", 1));
    }
}
