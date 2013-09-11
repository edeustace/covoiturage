package dao;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 10/09/13
 * Time: 20:50
 * To change this template use File | Settings | File Templates.
 */
public interface Dao<T> {

    T get(String id);

    T save(T obj);

    T update(T obj);

    void init();
}
