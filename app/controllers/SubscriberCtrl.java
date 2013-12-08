package controllers;

import static play.data.Form.form;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import static com.google.common.collect.Sets.newHashSet;

import actors.Message;
import actors.MessagesHandler;
import models.*;
import models.Car.CarIsFullException;
import models.enums.Locomotion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import controllers.decorators.SubscriberModel;
import play.mvc.ServerSentEventChunk;

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
            String reponse = objectMapper.writeValueAsString(subscriberModel);
            Logger.debug("getSubscriber : idEvent: {}, idSub {} : {}", id, idSub, reponse);
            return ok(reponse).as("application/json");
        } catch (Exception e){
            Logger.error("getSubscriber idEvent : "+id+", idSub : "+idSub , e);
            return internalServerError().as("application/json");
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result createSubscriber(String id){
        try{
            Logger.debug("createSubscriber : idEvent: {}, requestBody {}", id, request().body().asJson());
            Form<Subscriber> form = subscriberForm.bindFromRequest();
            if(form.hasErrors()){
                Logger.debug("Error : "+form.errorsAsJson());
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
                MessagesHandler.notifySubscriberUpdated(id, subscriber.getUserRef(), subscriber, new Date());
                String reponse = objectMapper.writeValueAsString(subscriberModel);
                return ok().as("application/json");
            }
        } catch (Exception e){
        	Logger.error("createSubscriber idEvent : "+id, e);
            return internalServerError().as("application/json");
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateSubscriber(String id, String idSub){
        try{
            Logger.debug("updateSubscriber : idEvent: {}, idSub {}, requestBody {}", id, idSub, request().body().asJson());

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
                MessagesHandler.notifySubscriberUpdated(id, subsc.getUserRef(), subsc, new Date());
                SubscriberModel subscriberModel = new SubscriberModel(subsc, event.getId());
                return ok(objectMapper.writeValueAsString(subscriberModel)).as("application/json");
            }
        } catch (Exception e){
            Logger.error("updateSubscriber idEvent : "+id+", idSub : "+idSub, e);
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
            String reponse = objectMapper.writeValueAsString(models);
            Logger.debug("list : idEvent: {}, response {}", id, reponse);
            return ok(reponse).as("application/json");
        } catch (IOException e) {
            Logger.error("list idEvent : "+id, e);
            return internalServerError().as("application/json");
        }
    }


    @BodyParser.Of(BodyParser.Json.class)
    public static Result changeLocomotion(String id, String idSub){
        try{
            Logger.debug("changeLocomotion, idEvent : {}, idSub {}, locomotion : {}", id, idSub, request().body().asJson());
            Event event = Event.read(id);
            Subscriber subsc = event.getSubscriberById(idSub);
            JsonNode node = request().body().asJson();
            String locomotion = node.findPath("locomotion").textValue();
            if(locomotion!=null){
                if(locomotion.equals("CAR") && (subsc.getLocomotion().equals(Locomotion.AUTOSTOP) || subsc.getLocomotion().equals(Locomotion.DONT_KNOW_YET)) ){
                    if(subsc.getPossibleCars()!=null){
                        subsc.getPossibleCars().clear();
                    }
                    for(Subscriber subscriber : event.getSubscribers()){
                        if(subscriber.getCar()!=null){
                            if( subscriber.getCar().getWaiting()!=null){
                                subscriber.getCar().getWaiting().remove(idSub);
                            }
                            if( subscriber.getCar().getPassengers()!=null){
                                subscriber.getCar().getPassengers().remove(idSub);
                            }
                        }
                    }
                    subsc.setCar(new Car());
                    subsc.setCarRef(null);
                    subsc.setLocomotion(Locomotion.CAR);
                    MessagesHandler.notifySubscriberUpdated(id, subsc.getUserRef(), subsc, new Date());
                }else if(locomotion.equals("AUTOSTOP") && subsc.getLocomotion().equals(Locomotion.CAR) || subsc.getLocomotion().equals(Locomotion.DONT_KNOW_YET)){
                    event.removeCar(idSub);
                    subsc.setCar(null);
                    subsc.setLocomotion(Locomotion.AUTOSTOP);
                    MessagesHandler.notifySubscriberUpdated(id, subsc.getUserRef(), subsc, new Date());
                }
            }

            event.save();
            return ok().as("application/json");
        } catch (Exception e){
            Logger.error("changeLocomotion idEvent : "+id+", idSub : "+idSub, e);
            return internalServerError().as("application/json");
        }
    }

    public static Result getCar(String id, String idSub){
        try{
            Event event = Event.read(id);
            Subscriber subsc = event.getSubscriberById(idSub);
            String reponse = objectMapper.writeValueAsString(subsc.getCar());
            Logger.debug("getCar, idEvent : {}, idSub {}, car : {}", id, idSub, reponse);
            return ok(reponse).as("application/json");
        } catch (Exception e){
            Logger.error("getCar idEvent :", id+", idSub : "+idSub, e);
            return internalServerError().as("application/json");
        }
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateCar(String id, String idSub){
        try{
            Logger.debug("updateCar, idEvent : {}, idSub {}, passenger : {}", id, idSub, request().body().asJson());
            Event event = Event.read(id);
            Car car = event.getSubscriberById(idSub).getCar();
            Set<String> usersToNotify = newHashSet(car.getPassengers());

            JsonNode node = request().body().asJson();
            String idPassenger = node.findPath("passenger").textValue();
            event.addPassenger(idPassenger, idSub);
            event.update();
            String from = getCurrentUserRef();
            Subscriber sub = event.getSubscriberById(from);
            String to = getToUser(from, idSub, idPassenger);
            
            String[] args = {sub.getSurname(), sub.getName()};

            MessagesHandler.notifyCarUpdated(id, idSub, event.getSubscriberById(idSub).getCar(), usersToNotify);
            if(sub.getLocomotion().equals(Locomotion.CAR)){
            	MessageFormat msg = new MessageFormat("{0} {1} a validé votre demande et vous serez son passager");
            	msg.format(args);
                MessagesHandler.sendNotification(id, from, to, "success", msg.format(args), new Date());
            }else if(sub.getLocomotion().equals(Locomotion.AUTOSTOP)){
            	MessageFormat msg = new MessageFormat("{0} {1} a validé votre demande et sera votre passager");
            	msg.format(args);
                MessagesHandler.sendNotification(id, from, to, "success", msg.format(args), new Date());
            }

            List<Topic> topics = Topic.findByIdEventAndIdUser(id, idSub);
            for(Topic topic : topics){
                if(topic.categorie.equals("carChat")){
                    if(!topic.subscribers.contains(idPassenger)){
                        topic.subscribers.add(idPassenger);
                        topic.save();
                        MessagesHandler.publishTopic(topic, Message.Statut.UPDATED);
                        break;
                    }
                }
            }

            return ok().as("application/json");
        } catch (CarIsFullException e){
            Logger.debug("updateCar, idEvent :"+id+", idSub : "+idSub+", passenger : "+request().body().asJson(), e);
        	return badRequest("{message: 'La voiture est pleine'}").as("application/json");
        } catch (Exception e){
            Logger.error("updateCar, idEvent :"+id+", idSub : "+idSub+", passenger : "+request().body().asJson(), e);
            return internalServerError(e.getMessage()).as("application/json");
        }
    }

    public static Result deletePassenger(String id, String idSub, String idPassenger){
    	try{
            Logger.debug("deletePassenger, idEvent : {}, idSub {}, idPassenger : {}", id, idSub, idPassenger);
            Event event = Event.read(id);
            Car car = event.getSubscriberById(idSub).getCar();
            Set<String> subscribersToNotify = newHashSet(car.getPassengers());
	    	event.deletePassenger(idPassenger, idSub);
	    	event.update();
            String from = getCurrentUserRef();
            String to = getToUser(from, idSub, idPassenger);
            Subscriber sub = event.getSubscriberById(from);
            String[] args = {sub.getSurname(), sub.getName()};

            MessagesHandler.notifyCarUpdated(id, idSub, event.getSubscriberById(idSub).getCar(), subscribersToNotify);

            if(sub.getLocomotion().equals(Locomotion.CAR)){
            	MessageFormat msg = new MessageFormat("vous ne faites plus parti de la voiture de {0} {1}");
            	msg.format(args);
                MessagesHandler.sendNotification(id, from, to, "warning", msg.format(args), new Date());
            }else if(sub.getLocomotion().equals(Locomotion.AUTOSTOP)){
            	MessageFormat msg = new MessageFormat("{0} {1} ne fait plus parti de votre voiture");
            	msg.format(args);
                MessagesHandler.sendNotification(id, from, to, "warning", msg.format(args), new Date());
            }
            MessagesHandler.sendNotification(id, from, to, "deletePassenger", "", new Date());
	    	return ok().as("application/json");
	    } catch (Exception e){
            Logger.error("deletePassenger idEvent : "+id+", idSub : "+idSub+", idPassenger : "+idPassenger, e);
	        return internalServerError(e.getMessage()).as("application/json");
	    }
    }

    public static Result addToWaitingList(String id, String idSub){
        try{
            Logger.debug("addToWaitingList, idEvent : {}, idSub {}, passenger : {}", id, idSub, request().body().asJson());
            Event event = Event.read(id);
            JsonNode node = request().body().asJson();
            String idPassenger = node.findPath("passenger").textValue();
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
            MessagesHandler.sendNotification(id, from, to, "success", msg.format(args), new Date());
            return ok().as("application/json");
        } catch (Exception e){
            Logger.error("addToWaitingList idEvent : "+id+", idSub : "+idSub, e);
            return internalServerError(e.getMessage()).as("application/json");
        }
    }
    
    public static Result removeFromWaitingList(String id, String idSub, String idPassenger){
        try{
            Logger.debug("removeFromWaitingList, idEvent : {}, idSub {}, idPassenger : {}", id, idSub, idPassenger);
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
            MessagesHandler.sendNotification(id, from, to, "warning", msg.format(args), new Date());
            return ok().as("application/json");
        } catch (CarIsFullException e){
            Logger.error("removeFromWaitingList, la voiture est pleine", e);
        	return badRequest("{message: 'La voiture est pleine'}").as("application/json");
        } catch (Exception e){
            Logger.error("removeFromWaitingList idEvent : "+id+", idSub : "+idSub+", idPassenger :"+idPassenger, e);
            return internalServerError(e.getMessage()).as("application/json");
        }
    }
    
    
    public static Result deleteSubscriber(String id, String idSub){
	    return ok().as("application/json");
    }
    
    public static Result addPossibleCar(String id, String idSub){
        try{
            Logger.debug("addPossibleCar, idEvent : {}, idSub {}, car : {}", id, idSub, request().body().asJson());
            Event event = Event.read(id);
            JsonNode node = request().body().asJson();
            String idCar = node.findPath("car").textValue();
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
            MessagesHandler.sendNotification(id, from, to, "success", msg.format(args), new Date());
            return ok().as("application/json");
        } catch (Exception e){
            Logger.error("addPossibleCar  idEvent : "+id+", idSub : "+idSub, e);
            return internalServerError(e.getMessage()).as("application/json");
        }
    }

    @BodyParser.Of(BodyParser.AnyContent.class)
    public static Result deletePossibleCar(String id, String idSub, String idCar){
        try{
            Logger.debug("deletePossibleCar, idEvent : {}, idSub {}, idCar : {}", id, idSub, idCar);
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
            MessagesHandler.sendNotification(id, from, to, "warning", msg.format(args), new Date());
            return ok().as("application/json");
        } catch (Exception e){
            Logger.error("deletePossibleCar idEvent : "+id+", idSub : "+idSub+", idCar : "+idCar, e);
            return internalServerError(e.getMessage()).as("application/json");
        }
    }
    
    public static Result listNotifications(String idEvent, String idSub){
    	List<Notification> result = Notification.listNotifications(idEvent, idSub);
    	try {
            String reponse = objectMapper.writeValueAsString(result) ;
            Logger.debug("listNotifications, reponse : {}", reponse);
			return ok(reponse).as("application/json");
		} catch (IOException e) {
            Logger.error("listNotifications idEvent : "+idEvent+", idSub : "+idSub, e);
            return internalServerError(e.getMessage()).as("application/json");
		}
    }
    
    public static Result deleteNotifications(String id, String idSub, String idNotifications){
    	Notification.delete(idNotifications);
    	return ok().as("application/json");
    }

    public static Result pushChannel(final String idEvent, final String userRef){
        Chunks<String> chunks = new ServerSentEventChunk() {
            // Called when the stream is ready
            public void onReady(ServerSentEventChunk out) {
                try{
                    MessagesHandler.join(idEvent, userRef, out);
                }catch(Exception e){
                    Logger.error("", e);
                }
            }
        };
        return ok(chunks).as("text/event-stream");
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

