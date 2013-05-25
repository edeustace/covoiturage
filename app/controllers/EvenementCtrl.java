package controllers;

import java.io.IOException;

import models.Evenement;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import views.html.evenement;
import views.html.evenementCreation;

public class EvenementCtrl extends Controller {

    private static ObjectMapper objectMapper = new ObjectMapper();

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
        Evenement evenement = Evenement.insert(node);
		return ok(objectMapper.writeValueAsString(evenement)).as("application/json");
	}

    @BodyParser.Of(BodyParser.Json.class)
	public static Result updateEvenement(String id) throws IOException {
        RequestBody body = request().body();
        JsonNode node = body.asJson();
        Evenement evenement = Evenement.update(id, node);
        return ok(objectMapper.writeValueAsString(evenement)).as("application/json");
	}

	public static Result deleteEvenement(String id) {
		String json = "{message:toto}";
		return ok(json).as("application/json");
	}
}
