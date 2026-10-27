package com.hospital.util;

/**
 * Application-wide constants used across the presentation, model, and service layers.
 */
public final class AppConstants {

    // Private constructor prevents instantiation of this utility class
    private AppConstants() {
        throw novelInstantiationError();
    }

    private static UnsupportedOperationException novelInstantiationError() {
        return new UnsupportedOperationException("Constants class cannot be instantiated.");
    }

    public static final String APP_NAME = "Hospital Appointment Management System";
    public static final String APP_VERSION = "Phase 1 (v1.0.0)";
    public static final String HOSPITAL_NAME = "Metropolitan General Hospital";
    public static final String TAGLINE = "Compassionate Care & Seamless Scheduling";

    // Date/Time Formats
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";
}
