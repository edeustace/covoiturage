package controllers;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail.Body;
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
import play.Logger;
import play.data.Form;
import play.i18n.Lang;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.evenement;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static play.data.Form.form;

@SubjectPresent
public class EventCtrl extends Controller {

    private static ObjectMapper objectMapper = new ObjectMapper();
        private static Form<Event> eventForm = form(Event.class);

	public static Result list() {
		return ok();
	}
	
	@Dynamic(value="event")
	@Restrict(@Group(Application.USER_ROLE))
	public static Result evenement(String id) {
		return ok(evenement.render());
	}

	@Restrict(@Group(Application.USER_ROLE))
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
                Event event = form.get();
                event.save();
                sendMail(event, event.getContacts());
                String link = controllers.routes.EventCtrl.getEvent(event.getId()).toString();
                LigthEvent responseBody = new LigthEvent(event, Link.link(Link.SELF, link));
                return ok(objectMapper.writeValueAsString(responseBody)).as("application/json");
            }
        }catch (Exception e){
            return internalServerError().as("application/json");
        }
	}
	
	private static void sendMail(Event event, List<String> contacts){
		if(contacts!=null && !contacts.isEmpty()){
        	Mailer mailer = Mailer.getDefaultMailer();
        	final Lang lang = Lang.preferred(request().acceptLanguages());
        	final String langCode = lang.code();
        	
        	User user = User.findById(event.getCreatorRef());
        	String url = controllers.routes.EventCtrl.evenement(event.getId()).absoluteURL(request()).toString();
        	final String html = getEmailTemplate(
    				"views.html.email.event_email", langCode, url,
    				event.getName(), user.getName(), user.getSurname());
    		final String text = getEmailTemplate(
    				"views.txt.email.event_email", langCode, url,
    				event.getName(), user.getName(), user.getSurname());
        	final Body body = new Body(text, html);
        	String subject = user.getSurname() + " " + user.getName() + " vous invite à covoiturer !";
        	for (String email : contacts) {
        		mailer.sendMail(subject, body, email);
			}
        }
	}
	
	private static String getEmailTemplate(final String template,
			final String langCode, final String url, final String eventName,
			final String name, final String surname) {
		Class<?> cls = null;
		String ret = null;
		try {
			cls = Class.forName(template + "_" + langCode);
		} catch (ClassNotFoundException e) {
			Logger.warn("Template: '"
					+ template
					+ "_"
					+ langCode
					+ "' was not found! Trying to use English fallback template instead.");
		}
		if (cls == null) {
			try {
				cls = Class.forName(template + "_"
						+ "fr");
			} catch (ClassNotFoundException e) {
				Logger.error("Fallback template: '" + template + "_"
						+ "fr"
						+ "' was not found either!");
			}
		}
		if (cls != null) {
			Method htmlRender = null;
			try {
				htmlRender = cls.getMethod("render", String.class,
						String.class, String.class, String.class);
				ret = htmlRender.invoke(null, url, eventName, name, surname)
						.toString();

			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	@Restrict(@Group(Application.USER_ROLE))
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
	
	@Restrict(@Group(Application.USER_ROLE))
    @BodyParser.Of(BodyParser.Json.class)
	public static Result addContacts(String id) throws IOException {
        try{
        	
        	JsonNode node = request().body().asJson();
        	JsonNode jsonContacts = node.get("contacts");
        	List<String> contacts = new ArrayList<String>();
        	if(jsonContacts.isArray()){
        		for (JsonNode jsonNode : jsonContacts) {
        			contacts.add(jsonNode.asText());
				}
        	}
            Event event = Event.read(id);
            contacts = event.addContactsAndSave(contacts);
            sendMail(event, contacts);
            String link = controllers.routes.EventCtrl.getEvent(event.getId()).toString();
            LigthEvent responseBody = new LigthEvent(event, Link.link(Link.SELF, link));
            return ok(objectMapper.writeValueAsString(responseBody)).as("application/json");
        }catch (Exception e){
            return internalServerError().as("application/json");
        }
	}	
	
	@Restrict(@Group(Application.USER_ROLE))
    @BodyParser.Of(BodyParser.Json.class)
	public static Result securised(String id) throws IOException {
        try{
        	JsonNode node = request().body().asJson();
        	Boolean securised = node.get("value").getBooleanValue();
        	Event event = Event.read(id);
            event.setContactsOnly(securised);
            event.save();
        	String link = controllers.routes.EventCtrl.getEvent(event.getId()).toString();
            LigthEvent responseBody = new LigthEvent(event, Link.link(Link.SELF, link));
            return ok(objectMapper.writeValueAsString(responseBody)).as("application/json");
        }catch (Exception e){
            return internalServerError().as("application/json");
        }
	}	
	
	@Restrict(@Group(Application.USER_ROLE))
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

    public static Result controllerJs(String id){
    	return ok();
    	//return ok(views.js.event.controller.render(id));
    }
    
    public static class EventModel{
        private Event event;
        private List<Link> links = new ArrayList<Link>();

        public EventModel(Event event) {
            if(event==null){
                throw new IllegalArgumentException("Event is required");
            }
            this.event = event;
            this.links.add(Link.link(Link.SELF, controllers.routes.EventCtrl.getEvent(this.event.getId()).toString()));
            this.links.add(Link.link("page", controllers.routes.EventCtrl.evenement(this.event.getId()).toString()));
            this.links.add(Link.link("subscribers", controllers.routes.SubscriberCtrl.list(this.event.getId()).toString()));
            this.links.add(Link.link("contacts", controllers.routes.EventCtrl.addContacts(this.event.getId()).toString()));
            this.links.add(Link.link("securised", controllers.routes.EventCtrl.securised(this.event.getId()).toString()));
            this.links.add(Link.link("topics", controllers.routes.ChatController.createTopic(this.event.getId()).toString()));
            this.links.add(Link.link("pictoFinish", controllers.routes.Assets.at("icons/finish.png").toString()));
            this.links.add(Link.link("pictoCarDark", controllers.routes.Assets.at("icons/car_dark.png").toString()));
            this.links.add(Link.link("pictoCar", controllers.routes.Assets.at("icons/car_classic.png").toString()));
            this.links.add(Link.link("pictoMyCar", controllers.routes.Assets.at("icons/car_red.png").toString()));
            this.links.add(Link.link("pictoCarLight", controllers.routes.Assets.at("icons/car_light.png").toString()));
            this.links.add(Link.link("pictoMyPassenger", controllers.routes.Assets.at("icons/pedestriancrossing_red.png").toString()));
            this.links.add(Link.link("pictoStopDark", controllers.routes.Assets.at("icons/pedestriancrossing_green-dark.png").toString()));
            this.links.add(Link.link("pictoStop", controllers.routes.Assets.at("icons/pedestriancrossing_green-classic.png").toString()));
            this.links.add(Link.link("pictoStopLight", controllers.routes.Assets.at("icons/pedestriancrossing_light.png").toString()));
            this.links.add(Link.link("pictoDontKnow", controllers.routes.Assets.at("icons/symbol_blank.png").toString()));
            this.links.add(Link.link("pictoDontKnowLight", controllers.routes.Assets.at("icons/symbol_blank_jaune_def.png").toString()));
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
        @JsonProperty("contacts")
		public List<String> getContacts() {
			return event.getContacts();
		}
        @JsonProperty("contactsOnly")
		public Boolean getContactsOnly() {
			return event.getContactsOnly();
		}
        @JsonProperty("updated")
        public Boolean getUpdated() {
            return event.getUpdated();
        }
    }

    public static class LigthEvent{
        private Event event;
        private Link link;

        public LigthEvent(Event event, Link link) {
            this.event = event;
            this.link = link;
        }

        @JsonProperty("creatorRef")
        public String getCreatorRef() {
            return event.getCreatorRef();
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
