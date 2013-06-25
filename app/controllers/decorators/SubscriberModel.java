package controllers.decorators;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    private List<Link> links = new ArrayList<Link>();

    public SubscriberModel(Subscriber subscriber, String idEvent) {
        this.subscriber = subscriber;
        if(subscriber!=null){
            String get = controllers.routes.SubscriberCtrl.getSubscriber(idEvent, subscriber.getId()).toString();
            this.links.add(Link.link(Link.SELF, get));
            String create = controllers.routes.SubscriberCtrl.createSubscriber(idEvent).toString();
            this.links.add(Link.link(Link.CREATE, create));
            String update = controllers.routes.SubscriberCtrl.updateSubscriber(idEvent, subscriber.getId()).toString();
            this.links.add(Link.link(Link.UPDATE, update));
            String passengers = controllers.routes.SubscriberCtrl.addPassenger(idEvent, subscriber.getId()).toString();
            this.links.add(Link.link("passengers", passengers));
        }
    }

    @JsonProperty("links")
    public List<Link> getLink() {
        return links;
    }

    @JsonProperty("user")
    public UserModelLight getUser() {
    	
        return new UserModelLight(subscriber.getUser());
    }
    @JsonProperty("userRef")
    public String getUserRef() {
		return subscriber.getUserRef();
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
    @JsonProperty("passengers")
    public Set<String> getPassengers() {
		return subscriber.getPassengers();
	}
    @JsonProperty("car")
	public String getCar() {
		return subscriber.getCar();
	}
}
