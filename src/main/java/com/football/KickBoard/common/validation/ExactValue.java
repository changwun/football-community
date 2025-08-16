package com.football.KickBoard.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ExactValueValidator.class)
@Target({FIELD})
@Retention(RUNTIME)
public @interface ExactValue {

  String value();

  String message() default "값이 올바르지 않습니다.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}

