package com.hospital.dao;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * JAVA OOP & ARCHITECTURE CONCEPT: DATA ACCESS OBJECT (DAO) PATTERN & GENERICS
 * ============================================================================
 * The DAO pattern isolates the application/business layer from the persistence
 * layer (such as MySQL or files).
 *
 * This interface uses Java Generics (`T` for entity type, `ID` for ID type)
 * so that common CRUD (Create, Read, Update, Delete) methods are defined once
 * and reused across all repositories.
 *
 * NOTE: Full MySQL JDBC implementations will be plugged in during Phase 2.
 * ============================================================================
 *
 * @param <T>  The model entity type (e.g. Patient, Doctor, Appointment)
 * @param <ID> The primary key identifier type (e.g. Integer)
 */
public interface GenericDao<T, ID> {

    /**
     * Persists a new entity.
     *
     * @param entity the entity to save
     * @return the saved entity (typically with generated ID assigned)
     */
    T save(T entity);

    /**
     * Finds an entity by its unique ID.
     *
     * @param id the entity ID
     * @return Optional containing the entity if found, or empty Optional if not found
     */
    Optional<T> findById(ID id);

    /**
     * Retrieves all entities of this type.
     *
     * @return List of all entities
     */
    List<T> findAll();

    /**
     * Updates an existing entity.
     *
     * @param entity the entity with updated fields
     * @return the updated entity
     */
    T update(T entity);

    /**
     * Deletes an entity by its unique ID.
     *
     * @param id the ID of the entity to delete
     * @return true if deleted, false otherwise
     */
    boolean deleteById(ID id);
}
