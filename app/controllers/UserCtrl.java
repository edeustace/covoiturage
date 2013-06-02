package controllers;

import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 02/06/13
 * Time: 21:49
 * To change this template use File | Settings | File Templates.
 */
public class UserCtrl extends Controller {

    public static Result getUser(String id) {
        return ok().as("application/json");
    }


}
