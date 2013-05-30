package models;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.NotNull;

public class Location {
    @NotNull
	private String lat;
    @NotNull
    private String lng;

    public static Location location(){
        return new Location();
    }

    @JsonIgnore
    public Boolean isEmpty(){
        return lat==null && lng==null;
    }

    public void merge(Location other){
        if(!other.isEmpty()){
            if(other.getLat()!=null){
                this.setLat(other.getLat());
            }
            if(other.getLng()!=null){
                this.setLng(other.getLng());
            }
        }
    }

    @JsonProperty("lat")
    public String getLat(){
        return lat;
    }
    @JsonProperty("lat")
    public Location setLat(String lat){
        this.lat = lat;
        return this;
    }
    @JsonProperty("lng")
    public String getLng(){
        return lng;
    }
    @JsonProperty("lng")
    public Location setLng(String lng){
        this.lng = lng;
        return this;
    }
}
