package controllers.decorators;

import models.Address;
import models.Subscriber;
import models.enums.Locomotion;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 02/06/13
 * Time: 21:37
 * To change this template use File | Settings | File Templates.
 */
public class SubscriberModel {

    private Subscriber subscriber;

    private Link link;

    public SubscriberModel(Subscriber subscriber, String idEvent) {
        this.subscriber = subscriber;
        if(subscriber!=null){
            String link = controllers.routes.SubscriberCtrl.getSubscriber(idEvent, subscriber.getId()).toString();
            this.link = Link.link(Link.SELF, link);
        }
    }

    @JsonProperty("link")
    public Link getLink() {
        return link;
    }

    @JsonProperty("user")
    public UserModelLight getUser() {
        return new UserModelLight(subscriber.getUser());
    }

    @JsonProperty("name")
    public String getName() {
        return subscriber.getName();
    }

    @JsonProperty("surname")
    public String getSurname() {
        return subscriber.getSurname();
    }

    @JsonProperty("email")
    public String getEmail() {
        return subscriber.getEmail();
    }

    @JsonProperty("address")
    public Address getAddress() {
        return subscriber.getAddress();
    }

    @JsonProperty("locomotion")
    public Locomotion getLocomotion() {
        return subscriber.getLocomotion();
    }
}
