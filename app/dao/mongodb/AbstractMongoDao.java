package dao.mongodb;

import dao.Dao;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;
import play.modules.mongodb.jackson.MongoDB;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 10/09/13
 * Time: 20:41
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractMongoDao <T> implements Dao<T> {

    private JacksonDBCollection<T, String> collection = null;

    public T update(String id, T obj) {
        return getCollection().updateById(id, obj).getSavedObject();
    }

    public AbstractMongoDao() {
        this.collection = MongoDB.getCollection(getClassOfObject(), String.class);
    }

    public JacksonDBCollection<T, String> getCollection() {
        return collection;
    }

    protected abstract Class<T> getClassOfObject();

    public T save(T data){
        WriteResult<T, String> result = this.getCollection().save(data);
        return  result.getSavedObject();
    }

    public T get(String id){
        return getCollection().findOneById(id);
    }
}
