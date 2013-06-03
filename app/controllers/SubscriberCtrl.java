package controllers;

import controllers.decorators.SubscriberModel;
import models.Event;
import models.Subscriber;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static play.data.Form.form;

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
        return ok().as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result createSubscriber(String id){
        Form<Subscriber> form = subscriberForm.bindFromRequest();
        if(form.hasErrors()){
            return badRequest(form.errorsAsJson()).as("application/json");
        } else {
            Subscriber subscriber = form.get();
            Event event = Event.read(id);
            event.addSubscriber(subscriber);
            event.save();
            return ok().as("application/json");
        }
    }

    public static Result list(String id){
        Event event = Event.read(id);
        Collection<Subscriber> subscribers = event.getSubscribers();
        Collection<SubscriberModel> models = new ArrayList<SubscriberModel>();
        for(Subscriber subscriber : subscribers){
            models.add(new SubscriberModel(subscriber));
        }
        try {
            return ok(objectMapper.writeValueAsString(models)).as("application/json");
        } catch (IOException e) {
            return internalServerError().as("application/json");
        }
    }

    public static Result updateSubscriber(String id){
        return ok().as("application/json");
    }

    public static Result updateSubscriber(String id, String idSub){
        return ok().as("application/json");
    }

    public static Result deleteSubscriber(String id, String idSub){
        return ok().as("application/json");
    }
}

