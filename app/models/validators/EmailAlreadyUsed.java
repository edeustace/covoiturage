package models.validators;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import javax.validation.*;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = EmailAlreadyUsedValidator.class)
@play.data.Form.Display(name="constraint.uniqueemail")
public @interface EmailAlreadyUsed {
    String message() default EmailAlreadyUsedValidator.message;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

