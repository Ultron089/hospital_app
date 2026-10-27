package com.hospital.exception;

/**
 * Thrown when user input or entity attributes fail validation constraints
 * (e.g., blank required fields, invalid phone numbers, negative fees).
 */
public class ValidationException extends HospitalException {

    private final String fieldName;

    public ValidationException(String message) {
        super(message);
        this.fieldName = "";
    }

    public ValidationException(String fieldName, String message) {
        super(String.format("Validation error on field '%s': %s", fieldName, message));
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
