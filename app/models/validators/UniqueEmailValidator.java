package models.validators;

import models.User;
import play.libs.F;

import javax.validation.ConstraintValidator;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 03/06/13
 * Time: 22:10
 * To change this template use File | Settings | File Templates.
 */
public class UniqueEmailValidator extends play.data.validation.Constraints.Validator<Object>
        implements ConstraintValidator<UniqueEmail, Object> {
    /* Default error message */
    final static public String message = "error.uniqueemail";

    @Override
    public void initialize(UniqueEmail uniqueEmailValidator) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isValid(Object o) {
        if(o instanceof String){
            String email = (String)o;
            Boolean exists = User.isUserWithEmailExists(email);
            return !exists;
        }
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public F.Tuple<String, Object[]> getErrorMessageKey() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
