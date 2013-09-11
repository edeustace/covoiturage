package models;

import net.vz.mongodb.jackson.ObjectId;

import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 11/09/13
 * Time: 19:28
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractModel {

    private String id;

    @Id
    @ObjectId
    public String getId() {
        return id;
    }

    @Id
    @ObjectId
    public void setId(String id) {
        this.id = id;
    }
}
