package dao.mongodb;

import com.mongodb.BasicDBObject;
import dao.UserDao;
import models.User;
import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;
import play.modules.mongodb.jackson.MongoDB;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 10/09/13
 * Time: 20:35
 * To change this template use File | Settings | File Templates.
 */
public class MongoDbUserDao extends AbstractMongoDao<User> implements UserDao {
    @Override
    protected Class<User> getClassOfObject() {
        return User.class;
    }

    public Boolean isUserWithEmailExists(String email){
        DBCursor<User> cursor = getCollection().find(DBQuery.is("email", email));
        return cursor.hasNext();
    }

        public User getUserwithEmail(String email){
        return getCollection().findOne(DBQuery.is("email",email));
    }

    @Override
    public void init() {
        getCollection().ensureIndex(new BasicDBObject("email", 1));
        BasicDBObject indexLinkedAccount = new BasicDBObject();
        indexLinkedAccount.put("linkedAccounts.providerUserId", 1);
        indexLinkedAccount.put("linkedAccounts.providerKey", 1);
        getCollection().ensureIndex(indexLinkedAccount);
    }

    @Override
    public User findByProvider(String idProvider, String providerKey) {
        return getCollection().findOne(DBQuery.and(DBQuery.is("active", true),
                DBQuery.is("linkedAccounts.providerUserId", idProvider),
                DBQuery.is("linkedAccounts.providerKey", providerKey)));  //To change body of implemented methods use File | Settings | File Templates.
    }


}
