package models;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

public class Car {
	@SuppressWarnings("serial")
	public static class CarIsFullException extends RuntimeException{}
	
	private Integer nbPlaces;
	
	private List<String> passengers = new ArrayList<>();
	private List<String> waiting = new ArrayList<>();

	public Car addPassenger(String passenger){
		if(getNbPlaces().equals(this.getPassengers().size())){
			throw new CarIsFullException();
		}
		if(this!=null && !this.getPassengers().contains(passenger)){
			this.getWaiting().remove(passenger);
			this.getPassengers().add(passenger);
    	}
		return this;
	}
	
	@JsonProperty("passengers")
	public List<String> getPassengers() {
		return passengers;
	}
	@JsonProperty("passengers")
	public void setPassengers(List<String> passengers) {
		this.passengers = passengers;
	}
	@JsonProperty("nbPlaces")
	public Integer getNbPlaces() {
		if(nbPlaces==null){
			nbPlaces = 5;
		}
		return nbPlaces;
	}
	@JsonProperty("nbPlaces")
	public void setNbPlaces(Integer nbPlaces) {
		this.nbPlaces = nbPlaces;
	}
	@JsonProperty("waiting")
	public List<String> getWaiting() {
		return waiting;
	}
	@JsonProperty("waiting")
	public void setWaiting(List<String> waiting) {
		this.waiting = waiting;
	}
}