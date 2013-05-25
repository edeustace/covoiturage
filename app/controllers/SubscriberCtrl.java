package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 25/05/13
 * Time: 16:59
 * To change this template use File | Settings | File Templates.
 */
public class SubscriberCtrl extends Controller {


    public static Result getSubscriber(String id, String idSub){
        return ok().as("application/json");
    }

    public static Result createSubscriber(String id){
        return ok().as("application/json");
    }

    public static Result updateSubscriber(String id, String idSub){
        return ok().as("application/json");
    }

    public static Result deleteSubscriber(String id, String idSub){
        return ok().as("application/json");
    }
}

