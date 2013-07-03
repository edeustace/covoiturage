package actors;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.HashMap;
import java.util.Map;

import models.Subscriber;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Akka;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.WebSocket;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class SubscriberActor extends UntypedActor{

	static ActorRef subActorRef = Akka.system().actorOf(new Props(SubscriberActor.class));
	
	public static void join(final String idEvent, final String userRef, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception{
        
        // Send the Join message to the room
        String result = (String)Await.result(ask(subActorRef,new Join(idEvent, userRef, out), 1000), Duration.create(1, SECONDS));
        
        if("OK".equals(result)) {
            
            // For each event received on the socket,
            in.onMessage(new Callback<JsonNode>() {
               public void invoke(JsonNode event) {
            	   subActorRef.tell(event, null);               } 
            });
            
            // When the socket is closed.
            in.onClose(new Callback0() {
               public void invoke() {
                   
                   // Send a Quit message to the room.
            	   subActorRef.tell(new Quit(idEvent, userRef), null);
                   
               }
            });
            
        } else {
            
            // Cannot connect, create a Json error.
            ObjectNode error = Json.newObject();
            error.put("error", result);
            
            // Send the error to the socket.
            out.write(error);
            
        }
        
    }
	
	Map<String, Map<String, WebSocket.Out<JsonNode>>> events = new HashMap<String, Map<String, WebSocket.Out<JsonNode>>>();
	
	@Override
	public void onReceive(Object message) throws Exception {
		 if(message instanceof Join) {
            // Received a Join message
            Join join = (Join)message;
            
            Map<String, WebSocket.Out<JsonNode>> event = events.get(join.idEvent);
            
            // Check if this username is free.
            if(event==null) {
            	event = new HashMap<String, WebSocket.Out<JsonNode>>();
            	events.put(join.idEvent, event);
            } 
            
            if(!event.containsKey(join.userRef)){
            	event.put(join.userRef, join.channel);
            }
            
        } else if(message instanceof SubscriberMessage)  {
            
            // Received a Talk message
        	SubscriberMessage subscriber = (SubscriberMessage)message;
            
            notifyAll(subscriber.idEvent, subscriber.userRef, subscriber.subscriber);
            
        } else if(message instanceof Quit)  {
            
            // Received a Quit message
            Quit quit = (Quit)message;
            
            Map<String, WebSocket.Out<JsonNode>> event = events.get(quit.idEvent);
            if(event!=null){
            	event.remove(quit.userRef);
            }
        } else {
            unhandled(message);
        }
	}
	
	public static void notifyAll(String idEvent, String userRef, Subscriber message){
		
	}
	
	public static class Join {
        
        final String idEvent;
        final String userRef;
        final WebSocket.Out<JsonNode> channel;
        
        public Join(String idEvent, String userRef, WebSocket.Out<JsonNode> channel) {
            this.idEvent = idEvent;
            this.userRef = userRef;
            this.channel = channel;
        }
        
    }
	public static class Quit {
        
        final String idEvent;
        final String userRef;
        
		public Quit(String idEvent, String userRef) {
			super();
			this.idEvent = idEvent;
			this.userRef = userRef;
		}
    }
	
	public static class SubscriberMessage {
        final String idEvent;
        final String userRef;
        final Subscriber subscriber;
        
		public SubscriberMessage(String idEvent, String userRef,
				Subscriber subscriber) {
			super();
			this.idEvent = idEvent;
			this.userRef = userRef;
			this.subscriber = subscriber;
		}
	}

}
