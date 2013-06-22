package controllers;

import static play.data.Form.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import models.Address;
import models.Event;
import models.Subscriber;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.evenement;
import views.html.evenementParticipation;
import controllers.decorators.Link;
import controllers.decorators.SubscriberModel;
import controllers.decorators.UserModelLight;

public class EventCtrl extends Controller {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Form<Event> eventForm = form(Event.class);
    
	public static Result list() {
		return ok();
	}
	public static Result evenement(String id) {
		return ok(evenement.render());
	}
	public static Result participer(String id) {
		return ok(evenementParticipation.render());
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
                return buildErrors(form);
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
        try{
            Form<Event> form = eventForm.bindFromRequest();
            if(form.hasErrors()){
                return buildErrors(form);
            } else {
                Event event = form.get();
                Event eventInDb = Event.read(id);
                eventInDb.merge(event);
                eventInDb.save();
                String link = controllers.routes.EventCtrl.getEvent(eventInDb.getId()).toString();
                LigthEvent responseBody = new LigthEvent(eventInDb, Link.link(Link.SELF, link));
                return ok(objectMapper.writeValueAsString(responseBody)).as("application/json");
            }
        }catch (Exception e){
            return internalServerError().as("application/json");
        }
	}

	public static Result deleteEvent(String id) {
		String json = "{message:toto}";
		return ok(json).as("application/json");
	}

    private static Result buildErrors(Form<?> form){
        try{
            Map<String, Object> errors = new HashMap<String, Object>();
            errors.put("errors", form.errorsAsJson());
            return badRequest(objectMapper.writeValueAsString(errors)).as("application/json");
        }catch (Exception e){
            return internalServerError().as("application/json");
        }
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
                SubscriberModel model = new SubscriberModel(subscriber, this.event.getId());
                models.add(model);
            }
            return models;
        }

        @JsonProperty("creator")
        public UserModelLight getCreator() {
            return new UserModelLight(event.loadCreator());
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
