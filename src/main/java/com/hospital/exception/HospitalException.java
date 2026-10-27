package com.hospital.exception;

/**
 * ============================================================================
 * JAVA EXCEPTION CONCEPT: CHECKED EXCEPTIONS & INHERITANCE
 * ============================================================================
 * A custom exception class enables domain-specific error handling.
 * 
 * By extending `java.lang.Exception`, `HospitalException` is a CHECKED exception,
 * which means the compiler forces callers to handle it (using try-catch or throws).
 * ============================================================================
 */
public class HospitalException extends Exception {

    public HospitalException(String message) {
        super(message);
    }

    public HospitalException(String message, Throwable cause) {
        super(message, cause);
    }
}
