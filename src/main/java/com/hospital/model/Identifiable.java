package com.hospital.model;

/**
 * ============================================================================
 * JAVA OOP CONCEPT: INTERFACE
 * ============================================================================
 * An Interface is an abstract contract in Java that specifies WHAT a class
 * should do, without defining HOW it does it.
 *
 * Any class implementing this interface guarantees that it has an identifier (ID).
 * This makes it easy to write generic code that can handle any entity with an ID.
 * ============================================================================
 */
public interface Identifiable {

    /**
     * Retrieves the unique identifier of the entity.
     *
     * @return the unique integer ID
     */
    int getId();

    /**
     * Sets the unique identifier of the entity.
     *
     * @param id the unique integer ID
     */
    void setId(int id);
}
