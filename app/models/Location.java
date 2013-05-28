package models;

import org.codehaus.jackson.annotate.JsonProperty;

public class Location {
	private String lat;
	private String lng;

    public static Location location(){
        return new Location();
    }

    public Boolean empty(){
        return lat==null && lng==null;
    }

    public void merge(Location other){
        if(!other.empty()){
            if(other.lat()!=null){
                this.lat(other.lat());
            }
            if(other.lng()!=null){
                this.lng(other.lng());
            }
        }
    }

    @JsonProperty("lat")
    public String lat(){
        return lat;
    }
    @JsonProperty("lat")
    public Location lat(String lat){
        this.lat = lat;
        return this;
    }
    @JsonProperty("lng")
    public String lng(){
        return lng;
    }
    @JsonProperty("lng")
    public Location lng(String lng){
        this.lng = lng;
        return this;
    }

}
