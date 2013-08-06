package controllers.decorators;

import java.util.ArrayList;
import java.util.List;

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

    private String idEvent;
    
    public SubscriberModel(Subscriber subscriber, String idEvent) {
        this.subscriber = subscriber;
        this.idEvent = idEvent;
        if(subscriber!=null){
            this.links.add(Link.link(Link.SELF, controllers.routes.SubscriberCtrl.getSubscriber(idEvent, subscriber.getUserRef()).toString()));
            this.links.add(Link.link(Link.CREATE, controllers.routes.SubscriberCtrl.createSubscriber(idEvent).toString()));
            this.links.add(Link.link(Link.UPDATE, controllers.routes.SubscriberCtrl.updateSubscriber(idEvent, subscriber.getUserRef()).toString()));
            this.links.add(Link.link("car", controllers.routes.SubscriberCtrl.updateCar(idEvent, subscriber.getUserRef()).toString()));
            this.links.add(Link.link("locomotion", controllers.routes.SubscriberCtrl.changeLocomotion(idEvent, subscriber.getUserRef()).toString()));
            this.links.add(Link.link("addPossibleCar", controllers.routes.SubscriberCtrl.addPossibleCar(idEvent, subscriber.getUserRef()).toString()));
            this.links.add(Link.link("deletePossibleCar", controllers.routes.SubscriberCtrl.addPossibleCar(idEvent, subscriber.getUserRef()).toString()));
            this.links.add(Link.link("notifications", controllers.routes.SubscriberCtrl.listNotifications(idEvent, subscriber.getUserRef()).toString()));
            this.links.add(Link.link("feed", controllers.routes.SubscriberCtrl.pushChannel(idEvent, subscriber.getUserRef()).toString()));
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
	@JsonProperty("possibleCars")
    public List<String> getPossibleCars() {
		return subscriber.getPossibleCars();
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
    
    @JsonProperty("carRef")
    public String getCarRef() {
    	return subscriber.getCarRef();
    }
    @JsonProperty("car")
    public CarModel getCar() {
    	if(subscriber.getCar()==null){
    		return null;
    	}
		return new CarModel(subscriber.getCar(), this.idEvent, this.subscriber.getUserRef());
	}
}
