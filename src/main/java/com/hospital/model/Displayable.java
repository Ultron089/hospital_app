package com.hospital.model;

/**
 * ============================================================================
 * JAVA OOP CONCEPT: INTERFACE (POLYMORPHISM ENABLER)
 * ============================================================================
 * The Displayable interface defines a standardized way to retrieve user-facing
 * textual information for any entity in the Hospital System.
 * 
 * Subclasses implement this method differently (Polymorphism: multiple forms).
 * ============================================================================
 */
public interface Displayable {

    /**
     * Returns a formatted summary string suitable for display in UI lists and cards.
     *
     * @return Formatted human-readable string representation of this entity
     */
    String getDisplayInfo();
}
