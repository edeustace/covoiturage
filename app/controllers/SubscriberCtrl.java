package controllers;

import static play.data.Form.form;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import models.Car;
import models.Car.CarIsFullException;
import models.Event;
import models.Subscriber;
import models.User;
import models.enums.Locomotion;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import actors.SubscriberActor;
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
                SubscriberActor.notifySubscriberUpdate(id, subscriber.getUserRef(), subscriber);
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
                SubscriberActor.notifySubscriberUpdate(id, subsc.getUserRef(), subsc);
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
    
    public static Result getCar(String id, String idSub){
        try{
            Event event = Event.read(id);
            Subscriber subsc = event.getSubscriberById(idSub);
            return ok(objectMapper.writeValueAsString(subsc.getCar())).as("application/json");
        } catch (Exception e){
            return internalServerError().as("application/json");
        }
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateCar(String id, String idSub){
        try{
            Event event = Event.read(id);
            JsonNode node = request().body().asJson();
            String idPassenger = node.get("passenger").getTextValue();
            event.addPassenger(idPassenger, idSub);
            event.update();
            String from = getCurrentUserRef();
            Subscriber sub = event.getSubscriberById(from);
            String to = getToUser(from, idSub, idPassenger);
            
            String[] args = {sub.getSurname(), sub.getName()};
            if(sub.getLocomotion().equals(Locomotion.CAR)){
            	MessageFormat msg = new MessageFormat("{0} {1} a validé votre demande et vous serez son passager");
            	msg.format(args);
                SubscriberActor.notifySendDemand(id, from, to, "success", msg.format(args));	
            }else if(sub.getLocomotion().equals(Locomotion.AUTOSTOP)){
            	MessageFormat msg = new MessageFormat("{0} {1} a validé votre demande et sera votre passager");
            	msg.format(args);
                SubscriberActor.notifySendDemand(id, from, to, "success", msg.format(args));
            }
            
            return ok().as("application/json");
        } catch (CarIsFullException e){
        	return badRequest("{message: 'La voiture est pleine'}").as("application/json");
        } catch (Exception e){
            return internalServerError(e.getMessage()).as("application/json");
        }
    }
    
    public static Result deletePassenger(String id, String idSub, String idPassenger){
    	try{
	    	Event event = Event.read(id);
	    	event.deletePassenger(idPassenger, idSub);
	    	event.update();
            String from = getCurrentUserRef();
            String to = getToUser(from, idSub, idPassenger);
            Subscriber sub = event.getSubscriberById(from);
            String[] args = {sub.getSurname(), sub.getName()};
            if(sub.getLocomotion().equals(Locomotion.CAR)){
            	MessageFormat msg = new MessageFormat("vous ne faites plus parti de la voiture de {0} {1}");
            	msg.format(args);
                SubscriberActor.notifySendDemand(id, from, to, "error", msg.format(args));	
            }else if(sub.getLocomotion().equals(Locomotion.AUTOSTOP)){
            	MessageFormat msg = new MessageFormat("{0} {1} ne fait plus parti de votre voiture");
            	msg.format(args);
                SubscriberActor.notifySendDemand(id, from, to, "error", msg.format(args));
            }
            SubscriberActor.notifySendDemand(id, from, to, "deletePassenger", "");
	    	return ok().as("application/json");
	    } catch (Exception e){
	        return internalServerError(e.getMessage()).as("application/json");
	    }
    }

    public static Result addToWaitingList(String id, String idSub){
        try{
            Event event = Event.read(id);
            JsonNode node = request().body().asJson();
            String idPassenger = node.get("passenger").getTextValue();
            Subscriber subsc = event.getSubscriberById(idSub);
            Car car = subsc.getCar();
            if(car!=null){
            	if(!car.getWaiting().contains(idPassenger)){
            		car.getWaiting().add(idPassenger);	
            	}
            }
            event.update();
            //Message : 
            String from = getCurrentUserRef();
            String to = getToUser(from, idSub, idPassenger);
            Subscriber sub = event.getSubscriberById(from);
            String[] args = {sub.getSurname(), sub.getName()};
        	MessageFormat msg = new MessageFormat("{0} {1} souhaite être passager de votre voiture");
        	msg.format(args);
            SubscriberActor.notifySendDemand(id, from, to, "success", msg.format(args));	
            return ok().as("application/json");
        } catch (Exception e){
            return internalServerError(e.getMessage()).as("application/json");
        }
    }
    
    public static Result removeFromWaitingList(String id, String idSub, String idPassenger){
        try{
            Event event = Event.read(id);
            Subscriber subsc = event.getSubscriberById(idSub);
            Car car = subsc.getCar();
            if(car!=null){
            	car.getWaiting().remove(idPassenger);
            }
            event.update();
            String from = getCurrentUserRef();
            String to = getToUser(from, idSub, idPassenger);
            Subscriber sub = event.getSubscriberById(from);
            String[] args = {sub.getSurname(), sub.getName()};
        	MessageFormat msg = new MessageFormat("{0} {1} a décliné votre proposition être passager de votre voiture");
        	msg.format(args);
            SubscriberActor.notifySendDemand(id, from, to, "error", msg.format(args));
            return ok().as("application/json");
        } catch (CarIsFullException e){
        	return badRequest("{message: 'La voiture est pleine'}").as("application/json");
        } catch (Exception e){
            return internalServerError(e.getMessage()).as("application/json");
        }
    }
    
    
    public static Result deleteSubscriber(String id, String idSub){
	    return ok().as("application/json");
    }
    
    public static Result addPossibleCar(String id, String idSub){
        try{
            Event event = Event.read(id);
            JsonNode node = request().body().asJson();
            String idCar = node.get("car").getTextValue();
            Subscriber subsc = event.getSubscriberById(idSub);
            if(subsc!=null){
            	if(!subsc.getPossibleCars().contains(idCar)){
            		subsc.getPossibleCars().add(idCar);	
            	}
            }
            event.update();
            
            String from = getCurrentUserRef();
            String to = getToUser(from, idSub, idCar);
            Subscriber sub = event.getSubscriberById(from);
            String[] args = {sub.getSurname(), sub.getName()};
        	MessageFormat msg = new MessageFormat("{0} {1} vous propose d'être passager de sa voiture");
        	msg.format(args);
            SubscriberActor.notifySendDemand(id, from, to, "success", msg.format(args));
            return ok().as("application/json");
        } catch (Exception e){
            return internalServerError(e.getMessage()).as("application/json");
        }
    }
    
    public static Result deletePossibleCar(String id, String idSub, String idCar){
        try{
            Event event = Event.read(id);
            Subscriber subsc = event.getSubscriberById(idSub);
            if(subsc!=null){
            	subsc.getPossibleCars().remove(idCar);
            }
            event.update();
            String from = getCurrentUserRef();
            String to = getToUser(from, idSub, idCar);
            Subscriber sub = event.getSubscriberById(from);
            String[] args = {sub.getSurname(), sub.getName()};
        	MessageFormat msg = new MessageFormat("{0} {1} a décliné votre proposition d'être passager de sa voiture");
        	msg.format(args);
            SubscriberActor.notifySendDemand(id, from, to, "error", msg.format(args));
            return ok().as("application/json");
        } catch (Exception e){
            return internalServerError(e.getMessage()).as("application/json");
        }
    }
    
    /**
     * Websocket.
     */
    public static WebSocket<JsonNode> subscribersUpdates(final String idEvent, final String userRef) {
        return new WebSocket<JsonNode>() {
            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){
                // Join the chat room.
                try { 
                	SubscriberActor.join(idEvent, userRef, in, out);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }
    
    private static String getToUser(String from, String id1, String id2){
    	 if(from!=null){
            if(!from.equals(id1)){
            	return id1;
            }else if(!from.equals(id2)){
            	return id2;
            }
         }
    	 return null;
    }
    
    private static String getCurrentUserRef(){
    	AuthUser authUser = PlayAuthenticate.getUser(ctx());
    	if(authUser!=null){
    		final User u = User.findByAuthUserIdentity(authUser);
    		return u.getId();
    	}
    	return null;
    }
  
}

