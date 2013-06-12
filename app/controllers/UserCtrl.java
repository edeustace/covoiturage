package controllers;

import java.io.IOException;

import models.User;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import play.mvc.Controller;
import play.mvc.Result;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 02/06/13
 * Time: 21:49
 * To change this template use File | Settings | File Templates.
 */
public class UserCtrl extends Controller {

	private static ObjectMapper objectMapper = new ObjectMapper();
	
    public static Result getUser(String id) {
        return ok().as("application/json");
    }

    public static Result getCurrentUser() throws JsonGenerationException, JsonMappingException, IOException{
    	AuthUser authUser = PlayAuthenticate.getUser(ctx());
    	if(authUser!=null){
    		final User u = User.findByAuthUserIdentity(authUser);
    		return ok(objectMapper.writeValueAsString(u)).as("application/json");
    	}
    	
    	return ok().as("application/json");
    }

}
