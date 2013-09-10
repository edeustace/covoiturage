package dao.mongodb;

import com.mongodb.BasicDBObject;
import dao.ChatMessageDao;
import models.ChatMessage;
import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 10/09/13
 * Time: 21:27
 * To change this template use File | Settings | File Templates.
 */
public class MongoDbChatMessageDao extends AbstractMongoDao<ChatMessage> implements ChatMessageDao{

    @Override
    public void init() {
        getCollection().ensureIndex(new BasicDBObject("topicRef", 1));
    }

    @Override
    protected Class<ChatMessage> getClassOfObject() {
        return ChatMessage.class;
    }

    public List<ChatMessage> findByIdTopic(String idTopic){
        DBCursor<ChatMessage> cursor = getCollection().find(DBQuery.is("topicRef", idTopic));
        List<ChatMessage> result = new ArrayList<ChatMessage>();
        while(cursor.hasNext()){
            result.add(cursor.next());
        }
        return result;
    }

}
