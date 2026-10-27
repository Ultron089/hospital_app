package com.hospital.model;

/**
 * ============================================================================
 * JAVA ENUM CONCEPT:
 * ============================================================================
 * An `enum` (Enumeration) is a special Java data type that defines a fixed set
 * of named constants. It provides type-safety compared to using plain Strings
 * or arbitrary integers for state representations.
 * ============================================================================
 */
public enum AppointmentStatus {
    SCHEDULED("Scheduled", "#2563eb"),
    CONFIRMED("Confirmed", "#059669"),
    COMPLETED("Completed", "#10b981"),
    CANCELLED("Cancelled", "#ef4444"),
    RESCHEDULED("Rescheduled", "#d97706"),
    NO_SHOW("No Show", "#6b7280");

    private final String displayName;
    private final String badgeColorHex;

    AppointmentStatus(String displayName, String badgeColorHex) {
        this.displayName = displayName;
        this.badgeColorHex = badgeColorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeColorHex() {
        return badgeColorHex;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
