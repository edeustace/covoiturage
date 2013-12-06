package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import static com.google.common.collect.Lists.newArrayList;
import models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.Validate;
import play.Logger;
import play.data.Form;
import play.libs.Akka;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.ServerSentEventChunk;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.util.*;

import static akka.pattern.Patterns.ask;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;
import static java.util.concurrent.TimeUnit.SECONDS;
import static play.data.Form.form;

public class MessagesHandler extends UntypedActor{

    private static Form<Topic> topicForm = form(Topic.class);
    private static Form<ChatMessage> messageForm = form(ChatMessage.class);

	static ActorRef subActorRef = Akka.system().actorOf(Props.create(MessagesHandler.class));
	
	public static void notifySubscriberUpdated(final String idEvent, final String userRef, Subscriber subscriber, Date date){
        subActorRef.tell(new Message(idEvent, "subscriber", Message.Statut.UPDATED, null, userRef, new ArrayList<String>()), null);
	}
	
	public static void sendNotification(final String idEvent, final String from, final String to, final String type, final String message, final Date date){
		Notification msg = new Notification(idEvent, from, to, type, message, date);
        subActorRef.tell(new Message(idEvent, "notification", Message.Statut.NA, msg, from, of(to)), null);
	}

    public static void notifyCarUpdated(String idEvent, String idCarOwner, Car car, Set<String> subscribersToNotify){
        subActorRef.tell(new CarUpdated(idEvent, idCarOwner, car, subscribersToNotify), null);
    }

    public static void publishTopic(Topic topic, Message.Statut action){
        subActorRef.tell(new Message(topic.idEvent, "topic", action, topic, topic.creator, copyOf(topic.subscribers)), null);
    }

    public static void publishChatMessage(ChatMessage message){
        subActorRef.tell(new Message(message.topic.idEvent, "message", Message.Statut.CREATED, message, message.from, copyOf(message.topic.subscribers)), null);
        subActorRef.tell(message, null);
    }

    public static void publishMessage(Message message){
        subActorRef.tell(message, null);
    }

	public static void join(final String idEvent, final String userRef, ServerSentEventChunk out) throws Exception{
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
            join(join);
        }else if(message instanceof CarUpdated)  {
            handleCarUpdated((CarUpdated) message);
        }else if(message instanceof Quit)  {
            Quit quit = (Quit)message;
            Map<String, ServerSentEventChunk> event = events.get(quit.idEvent);
            if(event!=null){
            	event.remove(quit.userRef);
            }
        }else if(message instanceof Message) {
             dispatchMessage((Message) message);
        }else {
            unhandled(message);
        }
	}

    public void dispatchMessage(Message message){
        Map<String, ServerSentEventChunk> users = events.get(message.idEvent);
        if(users!=null){
            if(message.to==null || message.to.isEmpty()){
                for(ServerSentEventChunk socket : users.values()){
                    try{
                        socket.sendMessage(mapper.writeValueAsString(message));
                    }catch(IOException e){
                        Logger.error("Erreur à la serialisation du message", e);
                    }
                }
            }else{
                for(String user : message.to){
                    ServerSentEventChunk socket = users.get(user);
                    if(socket!=null){
                        try{
                            socket.sendMessage(mapper.writeValueAsString(message));
                        }catch(IOException e){
                            Logger.error("Erreur à la serialisation du message", e);
                        }
                    }
                }
            }
        }
    }

    public void join(Join join){
        Map<String, ServerSentEventChunk> event = events.get(join.idEvent);

        if(event==null) {
            event = new HashMap<String, ServerSentEventChunk>();
            events.put(join.idEvent, event);
        }
        if(event.containsKey(join.userRef)){
            event.remove(join.userRef);
        }
        event.put(join.userRef, join.connection);
        getSender().tell("OK", getSelf());
    }


    public void handleCarUpdated(CarUpdated car){
        Validate.notNull(car);
        Topic topic = Topic.findByIdEventCategorieAndCreator(car.idEvent, Topic.TopicCategorie.carChat, car.idCarOwner);
        if(topic.getSubscribers()!=null && car.car!=null && car.car.getPassengers()!=null &&
                !Topic.subscribersEquals(topic.getSubscribers(), car.car.getPassengers())){
            topic.subscribers = car.car.getPassengers();
            topic.subscribers.add(car.idCarOwner);
            topic.update();
            car.subscribersToNotify.addAll(car.car.getPassengers());
            publishMessage(new Message(car.idEvent, "topic", Message.Statut.UPDATED, topic, topic.creator, newArrayList(car.subscribersToNotify)));

            publishTopic(topic, Message.Statut.UPDATED);
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

    public static class CarUpdated {
        public String idEvent;
        public String idCarOwner;
        public Car car;
        public Set<String> subscribersToNotify;

        public CarUpdated(String idEvent, String idCarOwner, Car car, Set<String> subscribersToNotify) {
            this.idEvent = idEvent;
            this.idCarOwner = idCarOwner;
            this.subscribersToNotify = subscribersToNotify;
            this.car = car;
        }
    }
}
