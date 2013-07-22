package providers;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.FirstLastNameIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import providers.MyUsernamePasswordAuthProvider.MySignup;

public class MyUsernamePasswordAuthUser extends UsernamePasswordAuthUser
		implements FirstLastNameIdentity {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String firstName;
    private final String lastName;

	public MyUsernamePasswordAuthUser(final MySignup signup) {
		super(signup.password, signup.email);
		this.firstName = signup.getFirstName();
        this.lastName = signup.getLastName();
    }

	/**
	 * Used for password reset only - do not use this to signup a user!
	 * @param password
	 */
	public MyUsernamePasswordAuthUser(final String password) {
		super(password, null);
		lastName = null;
        firstName = null;
	}

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getName() {
        return firstName + " " + lastName;
    }
}
