package models.validators;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import javax.validation.*;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
@play.data.Form.Display(name="constraint.uniqueemail")
public @interface UniqueEmail {
    String message() default UniqueEmailValidator.message;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

