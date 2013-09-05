package security;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import cache.CacheHandler;
import models.User;
import play.mvc.Http.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventPermissionHandler implements DynamicResourceHandler {

	Pattern[] patterns = {Pattern.compile("^/rest/events/([a-zA-Z0-9]+)(/.*)?"), Pattern.compile("^/evenement/([a-zA-Z0-9]+)(/.*)?")};
	
	@Override
	public boolean checkPermission(String arg0, DeadboltHandler arg1,
			Context arg2) {
		return true;
	}

	@Override
	public boolean isAllowed(String name,
            String meta,
            DeadboltHandler deadboltHandler,
            Context context) {
		
		String path = context.request().path();
		String idEvent = getEventId(path);
		if(idEvent!=null){
            CacheHandler.CachedEvent event = CacheHandler.getCachedEvent(idEvent);
			
			if(event!=null && event.getContactsOnly()){
				Subject subject = deadboltHandler.getSubject(context);
				String id = subject.getIdentifier();
				CacheHandler.CachedUser user = CacheHandler.getCachedUser(id);
				if(event.getMails().contains(user.email) || event.getIds().contains(user.id)){
					return true;
				}else{
					return false;
				}
			}
		}
		return true;
	}

	private String getEventId(String path){
		for (Pattern pattern : this.patterns) {
			Matcher matcher = pattern.matcher(path);
			if(matcher.matches()){
				String idEvent = matcher.group(1);
				return idEvent;
			}
		}
		return null;
	}

}
