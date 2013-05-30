package controllers;

import java.io.IOException;

import models.Event;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.data.Form;
import static play.data.Form.*;
import views.html.evenement;
import views.html.evenementCreation;

public class EvenementCtrl extends Controller {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Form<Event> eventForm = form(Event.class);

	public static Result list() {
		return ok();
	}

	public static Result evenement() {
		return ok(evenement.render());
	}

	public static Result evenementCreation() {
		return ok(evenementCreation.render());
	}

	public static Result getEvenement(String id) {
		ObjectNode result = Json.newObject();
		result.put("status", "OK");
		result.put("message", "Hello " + id);

		return ok(result);
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result createEvenement() throws IOException {
		RequestBody body = request().body();
		JsonNode node = body.asJson();

        Form<Event> form = eventForm.bindFromRequest();
        if(form.hasErrors()){
            return badRequest(form.errorsAsJson()).as("application/json");
        } else {
            Event event = Event.insert(node);
            return ok(objectMapper.writeValueAsString(event)).as("application/json");
        }
	}

    @BodyParser.Of(BodyParser.Json.class)
	public static Result updateEvenement(String id) throws IOException {
        RequestBody body = request().body();
        JsonNode node = body.asJson();
        Event event = Event.update(id, node);
        return ok(objectMapper.writeValueAsString(event)).as("application/json");
	}

	public static Result deleteEvenement(String id) {
		String json = "{message:toto}";
		return ok(json).as("application/json");
	}
}
