package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import controllers.decorators.Link;
import controllers.decorators.SubscriberModel;
import controllers.decorators.UserModelLight;
import models.Address;
import models.Event;

import models.Subscriber;
import models.User;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.data.Form;
import static play.data.Form.*;
import views.html.evenement;
import views.html.evenementCreation;

public class EventCtrl extends Controller {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Form<Event> eventForm = form(Event.class);

	public static Result list() {
		return ok();
	}

	public static Result evenement() {
		return ok(evenement.render());
	}

	public static Result evenementCreation() {
		return ok(evenementCreation.render());
	}

	public static Result getEvent(String id) {
	    Event event = Event.read(id);
        try {
            return ok(objectMapper.writeValueAsString(new EventModel(event))).as("application/json");
        } catch (IOException e) {
            return internalServerError("Error");
        }
    }

	@BodyParser.Of(BodyParser.Json.class)
	public static Result createEvent() {
		try{
            Form<Event> form = eventForm.bindFromRequest();
            if(form.hasErrors()){
                return badRequest(form.errorsAsJson()).as("application/json");
            } else {
                Event event = form.get().save();
                String link = controllers.routes.EventCtrl.getEvent(event.getId()).toString();
                LigthEvent responseBody = new LigthEvent(event, Link.link(Link.SELF, link));
                return ok(objectMapper.writeValueAsString(responseBody)).as("application/json");
            }
        }catch (Exception e){
            return internalServerError().as("application/json");
        }
	}

    @BodyParser.Of(BodyParser.Json.class)
	public static Result updateEvent(String id) throws IOException {
        RequestBody body = request().body();
        JsonNode node = body.asJson();
        Event event = Event.update(id, node);
        return ok(objectMapper.writeValueAsString(event)).as("application/json");
	}

	public static Result deleteEvent(String id) {
		String json = "{message:toto}";
		return ok(json).as("application/json");
	}

    public static class EventModel{
        private Event event;

        private Link link;

        public EventModel(Event event) {
            if(event==null){
                throw new IllegalArgumentException("Event is required");
            }
            this.event = event;
            this.link = Link.link(Link.SELF, controllers.routes.EventCtrl.getEvent(this.event.getId()).toString());
        }

        @JsonProperty("link")
        public Link getLink() {
            return link;
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

        @JsonProperty("address")
        public Address getAddress() {
            return event.getAddress();
        }

        @JsonProperty("subscribers")
        public Collection<SubscriberModel> getSubscribers() {
            Collection<SubscriberModel> models = new ArrayList<SubscriberModel>();
            for(Subscriber subscriber : event.getSubscribers()){
                SubscriberModel model = new SubscriberModel(subscriber);
                models.add(model);
            }
            return models;
        }

        @JsonProperty("creator")
        public UserModelLight getCreator() {
            return new UserModelLight(event.getCreator());
        }
    }

    public static class LigthEvent{
        private Event event;
        private Link link;

        public LigthEvent(Event event, Link link) {
            this.event = event;
            this.link = link;
        }

        @JsonProperty("name")
        public String getName() {
            return event.getName();
        }

        @JsonProperty("description")
        public String getDescription() {
            return event.getDescription();
        }

        @JsonProperty("id")
        public String getId() {
            return event.getId();
        }

        @JsonProperty("link")
        public Link link(){
            return link;
        }
    }
}
