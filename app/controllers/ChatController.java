package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import models.ChatMessage;
import models.Topic;
import net.vz.mongodb.jackson.MongoCollection;
import org.codehaus.jackson.map.ObjectMapper;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.util.List;

import static play.data.Form.form;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 25/07/13
 * Time: 20:35
 * To change this template use File | Settings | File Templates.
 */

public class ChatController extends Controller {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static Form<Topic> topicForm = form(Topic.class);


    public static Result getMainTopics(String idEvent, String categorie) {
        List<Topic> topics = Topic.findByIdEventAndCategorie(idEvent, categorie);
        try {
            return ok(objectMapper.writeValueAsString(topics)).as("application/json");
        } catch (IOException e) {
            return internalServerError(e.getMessage()).as("application/json");
        }
    }

    public static Result getTopics(String idEvent, String idUser) {
        List<Topic> topics = Topic.findByIdEventAndIdUser(idEvent, idUser);
        try {
            return ok(objectMapper.writeValueAsString(topics)).as("application/json");
        } catch (IOException e) {
            return internalServerError(e.getMessage()).as("application/json");
        }
    }

    public static Result getMessages(String idTopic) {
        List<ChatMessage> messages = ChatMessage.findByIdTopic(idTopic);
        try {
            return ok(objectMapper.writeValueAsString(messages)).as("application/json");
        } catch (IOException e) {
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
            topic.save();
            try {
                return ok(objectMapper.writeValueAsString(topic)).as("application/json");
            } catch (IOException e) {
                return internalServerError(e.getMessage()).as("application/json");
            }
        }

    }


}
