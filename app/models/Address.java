package models;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.NotNull;

public class Address {

    ///////////    FIELDS  /////////////////////
    @NotNull(message = "address.description.notNull")
	private String description;
    @NotNull(message = "address.location.notNull")
	private Location location;

    ///////////  CLASS METHODS /////////////////
    public void merge(Address other){
        if(!other.empty()){
            if(other.getDescription()!=null){
                this.setDescription(other.getDescription());
            }
            if(other.getLocation()!=null && !other.getLocation().isEmpty()){
                this.getLocation().merge(other.getLocation());
            }
        }
    }

    public Boolean empty(){
        return description==null && (location==null || location.isEmpty());
    }

    //////////////////////////////////////////////
    /////////        STATIC //////////////////////
    //////////////////////////////////////////////
    public static Address address() {
        return new Address();
    }

    //////////////////////////////////////////////
    /////////  GETTERS AND SETTERS ///////////////
    //////////////////////////////////////////////
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }
    @JsonProperty("description")
    public Address setDescription(String description) {
        this.description = description;
        return this;
    }
    @JsonProperty("location")
    public Location getLocation() {
        if(location==null){
            location = Location.location();
        }
        return location;
    }
    @JsonProperty("location")
    public Address setLocation(Location location) {
        this.location = location;
        return this;
    }
}
