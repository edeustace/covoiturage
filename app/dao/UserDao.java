package dao;

import models.User;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 10/09/13
 * Time: 20:40
 * To change this template use File | Settings | File Templates.
 */
public interface UserDao extends Dao<User> {
    Boolean isUserWithEmailExists(String email);

    User getUserwithEmail(String email);

    User findByProvider(String idProvider, String providerKey);

}
