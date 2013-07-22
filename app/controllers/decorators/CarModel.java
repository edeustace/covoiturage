package controllers.decorators;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

import models.Car;

public class CarModel {

	private Car car;

	private List<Link> links = new ArrayList<Link>();
	
	public CarModel(Car car, String idEvent, String idSub) {
		super();
		if(car==null){
			this.car = new Car();
		}else{
			this.car = car;	
		}
		this.links.add(Link.link(Link.SELF, controllers.routes.SubscriberCtrl.getCar(idEvent, idSub).toString()));
		this.links.add(Link.link("waitings", controllers.routes.SubscriberCtrl.addToWaitingList(idEvent, idSub).toString()));
	}

	@JsonProperty("links")
	public List<Link> getLinks() {
		return links;
	}
	@JsonProperty("passengers")
	public List<String> getPassengers() {
		return this.car.getPassengers();
	}
	@JsonProperty("nbPlaces")
	public Integer getNbPlaces() {
		Integer nbPlaces = this.car.getNbPlaces();
		if(nbPlaces==null){
			nbPlaces = 5;
		}
		return nbPlaces;
	}
	@JsonProperty("waitings")
	public List<String> getWaiting() {
		return car.getWaiting();
	}
}
