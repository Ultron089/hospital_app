package com.hospital.service;

import com.hospital.exception.HospitalException;
import com.hospital.model.Patient;

import java.util.List;

/**
 * ============================================================================
 * ARCHITECTURE CONCEPT: SERVICE LAYER (BUSINESS LOGIC)
 * ============================================================================
 * The Service layer acts as the coordinator between the Presentation (GUI)
 * layer and the Data Access Layer (DAO).
 *
 * It contains core business workflows, validation rules, notifications,
 * and transaction boundaries.
 * ============================================================================
 */
public interface PatientService {

    /**
     * Registers a new patient with required validations.
     *
     * @param patient patient entity to register
     * @return the registered patient
     * @throws HospitalException if validation fails
     */
    Patient registerPatient(Patient patient) throws HospitalException;

    /**
     * Retrieves all registered patients.
     *
     * @return list of patients
     */
    List<Patient> getAllPatients();

    /**
     * Finds a patient by ID.
     *
     * @param id patient ID
     * @return Patient entity
     * @throws HospitalException if patient not found
     */
    Patient getPatientById(int id) throws HospitalException;

    /**
     * Searches patients by name, email, or phone number.
     * Demonstrates lambda expressions in search filtering.
     *
     * @param query search query keyword
     * @return list of matching patients
     */
    List<Patient> searchPatients(String query);

    /**
     * Returns an unmodifiable Map of patients indexed by their ID.
     * Demonstrates the Java Collections Map concept for O(1) lookups.
     *
     * @return Map of ID to Patient
     */
    java.util.Map<Integer, Patient> getPatientMap();
}
