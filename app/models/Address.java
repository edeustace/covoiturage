package models;

public class Address {
	private String description;
	private Location location;

    public static Address address() {
        return new Address();
    }

    public void merge(Address other){
        if(!other.empty()){
            if(other.description()!=null){
                this.description(other.description());
            }
            if(!other.location().empty()){
                this.location().merge(other.location());
            }
        }
    }

    public Boolean empty(){
        return description==null && location==null && (location==null || location.empty());
    }

    public String description() {
        return description;
    }

    public Address description(String description) {
        this.description = description;
        return this;
    }

    public Location location() {
        if(location==null){
            location = Location.location();
        }
        return location;
    }

    public Address location(Location location) {
        this.location = location;
        return this;
    }
}
