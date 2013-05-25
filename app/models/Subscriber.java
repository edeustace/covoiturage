package models;

import net.vz.mongodb.jackson.DBRef;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 25/05/13
 * Time: 17:13
 * To change this template use File | Settings | File Templates.
 */
public class Subscriber {

    public DBRef<User, String> userRef;

    public String name;

    public String surname;

    public String email;

    public Address address;

}
