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

    public List<Event> listByUser(String idUser){
        DBCursor<Event> cursor = getCollection().find(DBQuery.or(DBQuery.is("subscribers.userRef", idUser), DBQuery.is("creatorRef", idUser)));
        List<Event> result = new ArrayList<Event>();
        while(cursor.hasNext()){
            result.add(cursor.next());
        }
        return result;
    }


    @Override
    public void init() {
        getCollection().ensureIndex(new BasicDBObject("subscribers.userRef", 1));
        getCollection().ensureIndex(new BasicDBObject("creatorRef", 1));
    }
}
