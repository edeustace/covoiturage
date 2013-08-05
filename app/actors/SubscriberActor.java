package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import controllers.decorators.SubscriberModel;
import models.ChatMessage;
import models.Notification;
import models.Subscriber;
import models.Topic;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import play.Logger;
import play.data.Form;
import play.libs.Akka;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.Results;
import play.mvc.ServerSentEventChunk;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;
import static play.data.Form.form;

public class SubscriberActor extends UntypedActor{

    private static Form<Topic> topicForm = form(Topic.class);
    private static Form<ChatMessage> messageForm = form(ChatMessage.class);

	static ActorRef subActorRef = Akka.system().actorOf(new Props(SubscriberActor.class));
	
	public static void notifySubscriberUpdate(final String idEvent, final String userRef, Subscriber subscriber, Date date){
		SubscriberMessage message = new SubscriberMessage(idEvent, userRef, subscriber, date);
		subActorRef.tell(message, null);
	}
	
	public static void sendNotification(final String idEvent, final String from, final String to, final String type, final String message, final Date date){
		Notification msg = new Notification(idEvent, from, to, type, message, date);
		subActorRef.tell(msg, null);
	}

    public static void publishTopic(Topic topic){
        subActorRef.tell(topic, null);
    }

    public static void publishMessage(ChatMessage message){
        subActorRef.tell(message, null);
    }

	public static void join(final String idEvent, final String userRef, ServerSentEventChunk out) throws Exception{
        
        // Send the Join message to the room
        String result = (String)Await.result(ask(subActorRef, new Join(idEvent, userRef, out), 1000), Duration.create(1, SECONDS));



        if("OK".equals(result)) {

            out.onDisconnected(new Callback0() {
                @Override
                public void invoke() throws Throwable {
                    subActorRef.tell(new Quit(idEvent, userRef), null);
                }
            });
        } else {
            
            // Cannot connect, create a Json error.
            ObjectNode error = Json.newObject();
            error.put("error", result);
            
            // Send the error to the socket.
            out.sendMessage(mapper.writeValueAsString(error));
            
        }
        
    }
	
	Map<String, Map<String, ServerSentEventChunk>> events = new HashMap<String, Map<String, ServerSentEventChunk>>();
	private static ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public void onReceive(Object message) throws Exception {
		 if(message instanceof Join) {
            // Received a Join message
            Join join = (Join)message;
            
            Map<String, ServerSentEventChunk> event = events.get(join.idEvent);
            
            // Check if this username is free.
            if(event==null) {
            	event = new HashMap<String, ServerSentEventChunk>();
            	events.put(join.idEvent, event);
            } 
            if(event.containsKey(join.userRef)){
            	event.remove(join.userRef);
            }
            event.put(join.userRef, join.connection);
            getSender().tell("OK", getSelf());
            
        } else if(message instanceof SubscriberMessage)  {
        	SubscriberMessage subscriber = (SubscriberMessage)message;
            notifyAll(subscriber.idEvent, subscriber.userRef, subscriber.subscriber);
        } else if(message instanceof Quit)  {
            Quit quit = (Quit)message;
            Map<String, ServerSentEventChunk> event = events.get(quit.idEvent);
            if(event!=null){
            	event.remove(quit.userRef);
            }
        } else if(message instanceof Notification)  {
        	Notification notification = (Notification)message;
            notifyOne(notification);
        } else if(message instanceof Topic) {
             sendTopic((Topic)message);
        } else if(message instanceof ChatMessage) {
             sendMessage((ChatMessage)message);
        } else if(message instanceof JsonNode) {
             JsonNode unTypedMesg = (JsonNode)message;
             if(unTypedMesg.get("type").asText().equals("topic")){
                 Form<Topic> form = topicForm.bind(unTypedMesg);
                 if(!form.hasErrors()){
                     Topic topic = form.get();
                     topic.save();
                     sendTopic(topic);
                 }
             }else if(unTypedMesg.get("type").asText().equals("message")){
                 Form<ChatMessage> form = messageForm.bind(unTypedMesg);
                 if(!form.hasErrors()){
                     ChatMessage msg = form.get();
                     msg.save();
                     sendMessage(msg);
                     Topic topic = Topic.getById(msg.topicRef);
                     topic.update = new Date();
                     topic.save();
                     //sendTopic(topic);
                 }
             }
        } else {
            unhandled(message);
        }
	}

    public void sendTopic(Topic topic){
        Map<String, ServerSentEventChunk> users = events.get(topic.idEvent);
        if(users!=null){
            if(topic.subscribers.isEmpty()){
                for(ServerSentEventChunk socket : users.values()){
                    try{
                        socket.sendMessage(mapper.writeValueAsString(topic));
                    }catch(IOException e){
                        Logger.error("Erreur à la serialisation du topic", e);
                    }
                }
            }else{
                for(String user : topic.subscribers){
                    ServerSentEventChunk socket = users.get(user);
                    if(socket!=null){
                        try{
                            socket.sendMessage(mapper.writeValueAsString(topic));
                        }catch(IOException e){
                            Logger.error("Erreur à la serialisation du topic", e);
                        }
                    }
                }
            }
        }
    }

    public void sendMessage(ChatMessage message){
        Map<String, ServerSentEventChunk> users = events.get(message.topic.idEvent);
        if(users!=null){
            if(message.topic.subscribers == null || message.topic.subscribers.isEmpty()){
                for(ServerSentEventChunk socket : users.values()){
                    try{
                        String chunk = mapper.writeValueAsString(message);
                        socket.sendMessage(chunk);
                    }catch(IOException e){
                        Logger.error("Erreur à la serialisation du message", e);
                    }
                }
            }else{
                for(String user : message.topic.subscribers){
                    ServerSentEventChunk socket = users.get(user);
                    if(socket!=null){
                        try{
                            String chunk = mapper.writeValueAsString(message);
                            socket.sendMessage(chunk);
                        }catch(IOException e){
                            Logger.error("Erreur à la serialisation du message", e);
                        }
                    }
                }
            }
        }
    }
	public void notifyAll(String idEvent, String userRef, Subscriber message){
		Map<String, ServerSentEventChunk> users = events.get(idEvent);
		if(users!=null){
			for (String ref : users.keySet()) {
                ServerSentEventChunk socket = users.get(ref);
				SubscriberModel model = new SubscriberModel(message, idEvent);
                try{
                    socket.sendMessage(mapper.writeValueAsString(model));
                }catch(IOException e){
                    Logger.error("Erreur à la serialisation du subscriber", e);
                }
			}
		}
	}
	
	public void notifyOne(Notification notification){
		Map<String, ServerSentEventChunk> users = events.get(notification.idEvent);
		notification.save();
		if(users!=null){
            ServerSentEventChunk socket = users.get(notification.to);
			if(socket!=null){
                try{
				    socket.sendMessage(mapper.writeValueAsString(notification));
                }catch(IOException e){
                    Logger.error("Erreur à la serialisation d''une notification", e);
                }
			}else{

			}
		}
	}
	
	public static class Join {
        
        final String idEvent;
        final String userRef;
        final ServerSentEventChunk connection;
        
        public Join(String idEvent, String userRef, ServerSentEventChunk out) {
            this.idEvent = idEvent;
            this.userRef = userRef;
            this.connection = out;
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
        final Date date;
        
		public SubscriberMessage(String idEvent, String userRef,
				Subscriber subscriber, Date date) {
			super();
			this.idEvent = idEvent;
			this.userRef = userRef;
			this.subscriber = subscriber;
            if(date==null){
                this.date = new Date();
            }else this.date = date;

		}
	}
}
