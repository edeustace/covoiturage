package controllers;

import static play.data.Form.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import models.Event;
import models.Subscriber;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.decorators.SubscriberModel;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 25/05/13
 * Time: 16:59
 * To change this template use File | Settings | File Templates.
 */
public class SubscriberCtrl extends Controller {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Form<Subscriber> subscriberForm = form(Subscriber.class);
    
    public static Result getSubscriber(String id, String idSub){
        try{
            Event event = Event.read(id);
            Subscriber subsc = event.getSubscriberById(idSub);
            SubscriberModel subscriberModel = new SubscriberModel(subsc, event.getId());
            return ok(objectMapper.writeValueAsString(subscriberModel)).as("application/json");
        } catch (Exception e){
            return internalServerError().as("application/json");
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result createSubscriber(String id){
        try{
            Form<Subscriber> form = subscriberForm.bindFromRequest();
            if(form.hasErrors()){
                return badRequest(form.errorsAsJson()).as("application/json");
            } else {
                Subscriber subscriber = form.get();
                Event event = Event.read(id);
                String idSub = event.getIdSubscriber(subscriber);
                if(idSub!=null){
                	ObjectNode result = Json.newObject();
                	result.put("id", idSub);
                	result.put("message", "Déjà existant");
                	result.put("status", "KO");
                	return badRequest(result);
                }
                event.addSubscriber(subscriber);
                event.update();
                Subscriber subsc = event.getSubscriberByMail(subscriber.getEmail());
                SubscriberModel subscriberModel = new SubscriberModel(subsc, event.getId());
                return ok(objectMapper.writeValueAsString(subscriberModel)).as("application/json");
            }
        } catch (Exception e){
        	e.printStackTrace();
            return internalServerError().as("application/json");
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateSubscriber(String id, String idSub){
        try{
            Form<Subscriber> form = subscriberForm.bindFromRequest();
            if(form.hasErrors()){
                return badRequest(form.errorsAsJson()).as("application/json");
            } else {
                Subscriber subscriber = form.get();
                Event event = Event.read(id);
                if(event.getSubscriberById(idSub)==null){
                    //Subscriber not exist
                    return badRequest("Subscriber not exist").as("application/json");
                }
                event.addAndMergeSubscriber(subscriber);
                event.update();
                Subscriber subsc = event.getSubscriberByMail(subscriber.getEmail());
                SubscriberModel subscriberModel = new SubscriberModel(subsc, event.getId());
                return ok(objectMapper.writeValueAsString(subscriberModel)).as("application/json");
            }
        } catch (Exception e){
            return internalServerError().as("application/json");
        }
    }

    public static Result list(String id){
        try {
            Event event = Event.read(id);
            Collection<Subscriber> subscribers = event.getSubscribers();
            Collection<SubscriberModel> models = new ArrayList<SubscriberModel>();
            for(Subscriber subscriber : subscribers){
                models.add(new SubscriberModel(subscriber, id));
            }
            return ok(objectMapper.writeValueAsString(models)).as("application/json");
        } catch (IOException e) {
            return internalServerError().as("application/json");
        }
    }
    
    public static Result changeCar(String id, String idSub){
        try{
            Event event = Event.read(id);
            String idCar = request().body().asText();
            Subscriber subsc = event.getSubscriberById(idSub);
            subsc.setCar(idCar);
            event.update();
            SubscriberModel subscriberModel = new SubscriberModel(subsc, event.getId());
            return ok(objectMapper.writeValueAsString(subscriberModel)).as("application/json");
        } catch (Exception e){
            return internalServerError().as("application/json");
        }
    }
    
    public static Result getPassengers(String id, String idSub){
        try{
            Event event = Event.read(id);
            Subscriber subsc = event.getSubscriberById(idSub);
            return ok(objectMapper.writeValueAsString(subsc.getPassengers())).as("application/json");
        } catch (Exception e){
            return internalServerError().as("application/json");
        }
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result addPassenger(String id, String idSub){
        try{
            Event event = Event.read(id);
            JsonNode node = request().body().asJson();
            String idPassenger = node.get("passenger").getTextValue();
            event.addPassenger(idPassenger, idSub);
            event.update();
            return ok().as("application/json");
        } catch (Exception e){
            return internalServerError(e.getMessage()).as("application/json");
        }
    }

    public static Result deletePassenger(String id, String idSub, String idPassenger){
    	try{
	    	Event event = Event.read(id);
	    	event.deletePassenger(idPassenger, idSub);
	    	event.update();
	    	return ok().as("application/json");
	    } catch (Exception e){
	        return internalServerError(e.getMessage()).as("application/json");
	    }
    }
    public static Result deleteSubscriber(String id, String idSub){
	    return ok().as("application/json");
    }
}

