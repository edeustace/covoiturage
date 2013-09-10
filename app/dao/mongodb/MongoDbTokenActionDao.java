package dao.mongodb;

import com.mongodb.BasicDBObject;
import dao.TokenActionDao;
import models.TokenAction;
import models.User;
import net.vz.mongodb.jackson.DBQuery;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 10/09/13
 * Time: 21:27
 * To change this template use File | Settings | File Templates.
 */
public class MongoDbTokenActionDao extends AbstractMongoDao<TokenAction> implements TokenActionDao{
    @Override
    public void init() {
        BasicDBObject tokenType = new BasicDBObject();
        tokenType.put("token", 1);
        tokenType.put("type", 1);
        getCollection().ensureIndex(tokenType);

        BasicDBObject targetUserType = new BasicDBObject();
        targetUserType.put("targetUser", 1);
        targetUserType.put("type", 1);
        getCollection().ensureIndex(targetUserType);
    }

    @Override
    protected Class<TokenAction> getClassOfObject() {
        return TokenAction.class;
    }

    public TokenAction findByToken(final String token, final TokenAction.Type type) {
        return getCollection().findOne(DBQuery.and(DBQuery.is("token", token), DBQuery.is("type", type)));
    }

    public void deleteByUser(final User u, final TokenAction.Type type) {
        getCollection().remove(DBQuery.and(DBQuery.is("targetUser", u.getId()), DBQuery.is("type", type)));
    }
}
