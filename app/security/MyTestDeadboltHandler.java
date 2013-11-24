package security;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import models.SecurityRole;
import models.UserPermission;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.SimpleResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyTestDeadboltHandler extends AbstractDeadboltHandler {

	@Override
	public F.Promise<SimpleResult> beforeAuthCheck(final Http.Context context) {
		// user is logged in
	    return null;
	}

	@Override
	public Subject getSubject(final Http.Context context) {
		return new Subject() {
            @Override
            public List<? extends Role> getRoles() {
                return Collections.singletonList(SecurityRole.securityRole().setId((new org.bson.types.ObjectId()).toString())
                        .setRoleName(controllers.Application.USER_ROLE));
            }

            @Override
            public List<? extends Permission> getPermissions() {
                return new ArrayList<UserPermission>();  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getIdentifier() {
                return "123456";  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
	}

	@Override
	public DynamicResourceHandler getDynamicResourceHandler(
			final Http.Context context) {
		return new TestDynamicHandler();
	}

	@Override
	public F.Promise<SimpleResult>  onAuthFailure(final Http.Context context,
			final String content) {
		// if the user has a cookie with a valid user and the local user has
		// been deactivated/deleted in between, it is possible that this gets
		// shown. You might want to consider to sign the user out in this case.
        return F.Promise.promise(new F.Function0<SimpleResult>() {
            @Override
            public SimpleResult apply() throws Throwable {
                return forbidden("Forbidden");
            }
        });
	}
}
