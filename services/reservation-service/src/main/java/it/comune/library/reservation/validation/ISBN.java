package it.comune.library.reservation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IsbnValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ISBN {

    String message() default "ISBN non valido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

