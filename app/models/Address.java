package models;

public class Address {
	public String description;
	public Location location;

    public static Address address() {
        return new Address();
    }

    public String description() {
        return description;
    }

    public Address description(String address) {
        this.description = description;
        return this;
    }

    public Location location() {
        return location;
    }

    public Address location(Location location) {
        this.location = location;
        return this;
    }
}
