package com.hospital.dao;

import com.hospital.model.Patient;

import java.util.List;

/**
 * Data Access Object contract for Patient entities.
 * Inherits standard CRUD methods from GenericDao and declares patient-specific queries.
 * Concrete JDBC implementation will be added in Phase 2.
 */
public interface PatientDao extends GenericDao<Patient, Integer> {

    /**
     * Searches patients by first or last name.
     *
     * @param query Search query text
     * @return List of matching patients
     */
    List<Patient> searchByName(String query);

    /**
     * Finds a patient by their unique contact phone number.
     *
     * @param phoneNumber phone number string
     * @return Patient if found, or null
     */
    Patient findByPhoneNumber(String phoneNumber);
}
