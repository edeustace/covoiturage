package controllers.decorators;

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
public class UserModelLight {

    private User user;
    private Link link;

    public UserModelLight(User user) {
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
        user.setId(id);
        return user;
    }

    @JsonProperty("id")
    public String getId() {
        return user.getId();
    }
    
    @JsonProperty("email")
    public String getEmail() {
        return user.getEmail();
    }
}
