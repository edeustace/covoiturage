package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import controllers.decorators.EventLight;
import controllers.decorators.UserModel;
import models.Event;
import models.User;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        try{
            final User u = User.findById(id);
            return ok(objectMapper.writeValueAsString(new UserModel(u))).as("application/json");
        }catch (IOException e){
            return internalServerError().as("application/json");
        }
    }

    @Restrict(@Group(Application.USER_ROLE))
    public static Result getCurrentUser() throws IOException{
    	AuthUser authUser = PlayAuthenticate.getUser(ctx());
    	if(authUser!=null){
    		final User u = User.findByAuthUserIdentity(authUser);
    		return ok(objectMapper.writeValueAsString(new UserModel(u))).as("application/json");
    	}
    	return ok().as("application/json");
    }

    @Restrict(@Group(Application.USER_ROLE))
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


    @Restrict(@Group(Application.USER_ROLE))
    public static Result listEvent(String id){
		try {
			return ok(objectMapper.writeValueAsString(toEventLight(Event.listByUser(id)))).as("application/json");
		} catch (IOException e) {
			return badRequest(e.getMessage());
		}
    }

    @Restrict(@Group(Application.USER_ROLE))
    public static Result listInvitations(String id){
        try {
            User user = User.findById(id);
            if(user==null || StringUtils.isEmpty(user.getEmail())){
                Logger.error("listInvitations user or user email is empty");
                return badRequest();
            }
            return ok(objectMapper.writeValueAsString(toEventLight(Event.listInvitedByEmail(user.getEmail())))).as("application/json");
        } catch (IOException e) {
            return badRequest(e.getMessage());
        }
    }

    private static List<EventLight> toEventLight(List<Event> list){
        List<EventLight> result = new ArrayList<EventLight>();
        if(list!=null){
            for (Event event : list) {
                EventLight model = new EventLight(event);
                result.add(model);
            }
        }
        return result;
    }

}
