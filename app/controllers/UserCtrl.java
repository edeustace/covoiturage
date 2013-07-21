package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controllers.decorators.Link;
import controllers.decorators.UserModel;
import controllers.decorators.UserModelLight;
import models.Event;
import models.User;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import controllers.decorators.EventLight;

import static play.data.Form.form;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 02/06/13
 * Time: 21:49
 * To change this template use File | Settings | File Templates.
 */
public class UserCtrl extends Controller {

	private static ObjectMapper objectMapper = new ObjectMapper();
    private static Form<User> userForm = form(User.class);

    public static Result getUser(String id) {
        return ok().as("application/json");
    }

    public static Result getCurrentUser() throws JsonGenerationException, JsonMappingException, IOException{
    	AuthUser authUser = PlayAuthenticate.getUser(ctx());
    	if(authUser!=null){
    		final User u = User.findByAuthUserIdentity(authUser);
    		return ok(objectMapper.writeValueAsString(new UserModel(u))).as("application/json");
    	}
    	return ok().as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateUser(String id) {
       try{
            Form<User> form = userForm.bindFromRequest();
            if(form.hasErrors()){
                return buildErrors(form);
            } else {
                User user = User.findById(id);
                user.merge(form.get());
                user.save();
                return ok().as("application/json");
            }
        }catch (Exception e){
            return internalServerError().as("application/json");
        }
    }

    private static Result buildErrors(Form<?> form){
        try{
            Map<String, Object> errors = new HashMap<String, Object>();
            errors.put("errors", form.errorsAsJson());
            return badRequest(objectMapper.writeValueAsString(errors)).as("application/json");
        }catch (Exception e){
            return internalServerError().as("application/json");
        }
    }


    public static Result listEvent(String id){
    	List<Event> list = Event.listByUser(id);
    	List<EventLight> result = new ArrayList<EventLight>();
    	for (Event event : list) {
			EventLight model = new EventLight(event);
			result.add(model);
		}
		try {
			return ok(objectMapper.writeValueAsString(result)).as("application/json");
		} catch (IOException e) {
			return badRequest(e.getMessage());
		}
    }
    
}
