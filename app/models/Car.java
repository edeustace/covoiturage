package models;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

public class Car {
	@SuppressWarnings("serial")
	public static class CarIsFullException extends RuntimeException{}
	
	private Integer nbPlaces;
	
	private List<String> passengers = new ArrayList<String>();
	private List<String> waitingList = new ArrayList<String>();

	public Car addPassenger(String passenger){
		if(getNbPlaces().equals(this.getPassengers().size())){
			throw new CarIsFullException();
		}
		if(this!=null && !this.getPassengers().contains(passenger)){
			this.getWaitingList().remove(passenger);
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
	@JsonProperty("waitingList")
	public List<String> getWaitingList() {
		return waitingList;
	}
	@JsonProperty("waitingList")
	public void setWaitingList(List<String> waiting) {
		this.waitingList = waiting;
	}
}