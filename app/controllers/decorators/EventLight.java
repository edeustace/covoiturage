package controllers.decorators;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

import models.Address;
import models.Event;

public class EventLight {
	private Event event;
    private List<Link> links = new ArrayList<Link>();

    public EventLight(Event event) {
        if(event==null){
            throw new IllegalArgumentException("Event is required");
        }
        this.event = event;
        this.links.add(Link.link(Link.SELF, controllers.routes.EventCtrl.getEvent(this.event.getId()).toString()));
        this.links.add(Link.link("page", controllers.routes.EventCtrl.evenement(this.event.getId()).toString()));
        this.links.add(Link.link("subscribers", controllers.routes.SubscriberCtrl.list(this.event.getId()).toString()));
        this.links.add(Link.link("pictoFinish", com.ee.assets.controllers.routes.Assets.at("icons/finish.png").toString()));
        this.links.add(Link.link("pictoCarDark", com.ee.assets.controllers.routes.Assets.at("icons/car_dark.png").toString()));
        this.links.add(Link.link("pictoCar", com.ee.assets.controllers.routes.Assets.at("icons/car_classic.png").toString()));
        this.links.add(Link.link("pictoCarLight", com.ee.assets.controllers.routes.Assets.at("icons/car_light.png").toString()));
        this.links.add(Link.link("pictoStopDark", com.ee.assets.controllers.routes.Assets.at("icons/pedestriancrossing_dark.png").toString()));
        this.links.add(Link.link("pictoStop", com.ee.assets.controllers.routes.Assets.at("icons/pedestriancrossing_classic.png").toString()));
        this.links.add(Link.link("pictoStopLight", com.ee.assets.controllers.routes.Assets.at("icons/pedestriancrossing_light.png").toString()));
        this.links.add(Link.link("pictoDontKnow", com.ee.assets.controllers.routes.Assets.at("icons/symbol_blank_jaune_dark.png").toString()));
        this.links.add(Link.link("pictoDontKnowLight", com.ee.assets.controllers.routes.Assets.at("icons/symbol_blank_jaune_def.png").toString()));
    }

    @JsonProperty("links")
    public List<Link> getLinks() {
        return links;
    }

    @JsonProperty("id")
    public String getId() {
        return event.getId();
    }

    @JsonProperty("name")
    public String getName() {
        return event.getName();
    }

    @JsonProperty("description")
    public String getDescription() {
        return event.getDescription();
    }

    @JsonProperty("fromDate")
    public Date getFromDate() {
        return event.getFromDate();
    }

    @JsonProperty("toDate")
    public Date getToDate() {
        return event.getToDate();
    }

    @JsonProperty("creatorRef")
    public String getCreatorRef() {
        return event.getCreatorRef();
    }

    @JsonProperty("address")
    public Address getAddress() {
        return event.getAddress();
    }

}
