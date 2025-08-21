package com.football.kick_board.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ExactValueValidator implements ConstraintValidator<ExactValue, String> {

  private String requiredValue;

  @Override
  public void initialize(ExactValue constraintAnnotation) {
    this.requiredValue = constraintAnnotation.value();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return false;
    }
    return value.equals(requiredValue);
  }
}
