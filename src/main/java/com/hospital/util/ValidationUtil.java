package com.hospital.util;

import com.hospital.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Utility class for validating model field formats and business rules.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9\\-\\s()]{7,15}$");

    private ValidationUtil() {
        // Prevent instantiation
    }

    public static void requireNonEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " cannot be empty.");
        }
    }

    public static void validateEmail(String email) throws ValidationException {
        if (email != null && !email.trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
                throw new ValidationException("email", "Invalid email format: " + email);
            }
        }
    }

    public static void validatePhoneNumber(String phone) throws ValidationException {
        if (phone != null && !phone.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
                throw new ValidationException("phoneNumber", "Invalid phone number format: " + phone);
            }
        }
    }

    public static void validateFutureDateTime(java.time.LocalDateTime dateTime, String fieldName) throws ValidationException {
        if (dateTime == null) {
            throw new ValidationException(fieldName, fieldName + " cannot be null.");
        }
        if (dateTime.isBefore(java.time.LocalDateTime.now())) {
            throw new ValidationException(fieldName, fieldName + " cannot be in the past.");
        }
    }

    public static void validatePositive(double value, String fieldName) throws ValidationException {
        if (value < 0.0) {
            throw new ValidationException(fieldName, fieldName + " cannot be negative.");
        }
    }

    public static void validateDateOfBirth(java.time.LocalDate dob) throws ValidationException {
        if (dob == null) {
            throw new ValidationException("dateOfBirth", "Date of birth cannot be empty.");
        }
        if (dob.isAfter(java.time.LocalDate.now())) {
            throw new ValidationException("dateOfBirth", "Date of birth cannot be in the future.");
        }
    }
}
