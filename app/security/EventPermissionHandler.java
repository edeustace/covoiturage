package security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Event;
import models.Subscriber;
import models.User;
import play.mvc.Http.Context;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;

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
			Event event = Event.read(idEvent);
			
			if(event!=null && event.getContactsOnly()){
				Subject subject = deadboltHandler.getSubject(context);
				String id = subject.getIdentifier();
				User user = User.findById(id);
				if(event.getContacts().contains(user.getEmail())){
					return true;
				}else if(event.getCreatorRef()!=null && event.getCreatorRef().equals(user.getId())){
					return true;
				}else{
					for (Subscriber subscriber : event.getSubscribers()) {
						if(subscriber.getUserRef()!=null && subscriber.getUserRef().equals(user.getId())){
							return true;
						}
					}
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
