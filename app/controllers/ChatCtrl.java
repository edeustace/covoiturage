package controllers;

import actors.Message;
import actors.MessagesHandler;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import models.ChatMessage;
import models.Topic;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import play.Logger;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static play.data.Form.form;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 25/07/13
 * Time: 20:35
 * To change this template use File | Settings | File Templates.
 */

public class ChatCtrl extends Controller {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static Form<Topic> topicForm = form(Topic.class);
    private static Form<ChatMessage> messageForm = form(ChatMessage.class);

    public static Result getTopics(String idEvent, String categorie, String idUser) {
        if(StringUtils.isNotEmpty(categorie)){
            List<Topic> topics = Topic.findByIdEventAndCategorie(idEvent, Topic.TopicCategorie.valueOf(categorie));
            try {
                return ok(objectMapper.writeValueAsString(topics)).as("application/json");
            } catch (IOException e) {
                Logger.error("Erreur", e);
                return internalServerError(e.getMessage()).as("application/json");
            }
        }
        if(StringUtils.isNotEmpty(idUser)){
            List<Topic> topics = Topic.findByIdEventAndIdUser(idEvent, idUser);
            try {
                return ok(objectMapper.writeValueAsString(topics)).as("application/json");
            } catch (IOException e) {
                Logger.error("Erreur", e);
                return internalServerError(e.getMessage()).as("application/json");
            }
        }
        return ok().as("application/json");
    }

    public static Result getMessages(String idTopic) {
        List<ChatMessage> messages = ChatMessage.findByIdTopic(idTopic);
        try {
            return ok(objectMapper.writeValueAsString(messages)).as("application/json");
        } catch (IOException e) {
            Logger.error("Erreur", e);
            return internalServerError(e.getMessage()).as("application/json");
        }
    }

    @Restrict(@Group(Application.USER_ROLE))
    public static Result createTopic(String idEvent) {
        Form<Topic> form = topicForm.bindFromRequest();
        if(form.hasErrors()){
            return badRequest(form.errorsAsJson()).as("application/json");
        }else{
            Topic topic = form.get();

            Topic existing = Topic.exists(topic);
            if(existing==null){
                topic.save();
            }else{
                topic = existing;
            }
            MessagesHandler.publishTopic(topic, Message.Statut.CREATED);
            try {
                return ok(objectMapper.writeValueAsString(topic)).as("application/json");
            } catch (IOException e) {
                Logger.error("Erreur", e);
                return internalServerError(e.getMessage()).as("application/json");
            }
        }

    }

    @Restrict(@Group(Application.USER_ROLE))
    public static Result updateTopic(String idEvent, String idTopic) {
        Form<Topic> form = topicForm.bindFromRequest();
        if(form.hasErrors()){
            return badRequest(form.errorsAsJson()).as("application/json");
        }else{
            Topic topic = form.get();

            Topic existing = Topic.getById(idTopic);
            if(existing==null){
                return badRequest("Topic not existing").as("application/json");
            }
            topic.setId(existing.getId());
            topic.save();
            MessagesHandler.publishTopic(topic, Message.Statut.UPDATED);
            try {
                return ok(objectMapper.writeValueAsString(topic)).as("application/json");
            } catch (IOException e) {
                Logger.error("Erreur", e);
                return internalServerError(e.getMessage()).as("application/json");
            }
        }
    }


    @Restrict(@Group(Application.USER_ROLE))
    @BodyParser.Of(BodyParser.Json.class)
    public static Result addSubscribers(String idEvent, String idTopic) {
        JsonNode node  = request().body().asJson();

        Topic topic = Topic.getById(idTopic);

        JsonNode subscribers = node.get("subscribers");
        if(subscribers!= null){
            for(JsonNode val : subscribers){
                String subscriber = val.asText();
                if(subscriber!=null && !topic.subscribers.contains(subscriber)){
                    topic.subscribers.add(subscriber);
                }
            }
            topic.save();
        }
        MessagesHandler.publishTopic(topic, Message.Statut.UPDATED);
        try {
            return ok(objectMapper.writeValueAsString(topic)).as("application/json");
        } catch (IOException e) {
            Logger.error("Erreur", e);
            return internalServerError(e.getMessage()).as("application/json");
        }
    }


    @Restrict(@Group(Application.USER_ROLE))
    public static Result createMessage(String idEvent, String idTopic) {
        Form<ChatMessage> form = messageForm.bindFromRequest();
        if(form.hasErrors()){
            return badRequest(form.errorsAsJson()).as("application/json");
        }else{
            ChatMessage message = form.get();
            message.date = new Date();
            message.save();
            Topic topic = Topic.getById(idTopic);
            topic.update = new Date();
            topic.save();
            MessagesHandler.publishChatMessage(message);
            try {
                return ok(objectMapper.writeValueAsString(message)).as("application/json");
            } catch (IOException e) {
                Logger.error("Erreur", e);
                return internalServerError(e.getMessage()).as("application/json");
            }
        }

    }
}
