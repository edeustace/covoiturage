package security;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import play.mvc.Http.Context;

public class TestDynamicHandler implements DynamicResourceHandler {

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
		return true;
	}
	
}
