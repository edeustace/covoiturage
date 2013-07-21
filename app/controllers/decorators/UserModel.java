package controllers.decorators;

import models.Address;
import models.User;
import net.vz.mongodb.jackson.ObjectId;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 02/06/13
 * Time: 21:45
 * To change this template use File | Settings | File Templates.
 */
public class UserModel {

    private User user;
    private Link link;

    public UserModel(User user) {
        this.user = user;
        if(user!=null){
            String link = controllers.routes.UserCtrl.getUser(user.getId()).toString();
            this.link = Link.link(Link.SELF, link);
        }
    }

    @JsonProperty("link")
    public Link getLink() {
        return link;
    }

    @Id
    @ObjectId
    public User setId(String id) {
        return user.setId(id);
    }

    @JsonProperty("id")
    public String getId() {
        return user.getId();
    }
    
    @JsonProperty("email")
    public String getEmail() {
        return user.getEmail();
    }

    @JsonProperty("address")
    public Address getAddress() {
        return user.getAddress();
    }

    @JsonProperty("surname")
    public String getSurname() {
        return user.getSurname();
    }

    @JsonProperty("name")
    public String getName() {
        return user.getName();
    }
}
