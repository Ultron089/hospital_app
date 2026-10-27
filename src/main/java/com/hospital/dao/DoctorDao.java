package com.hospital.dao;

import com.hospital.model.Doctor;

import java.util.List;

/**
 * Data Access Object contract for Doctor entities.
 * Inherits standard CRUD methods from GenericDao and declares doctor-specific queries.
 * Concrete JDBC implementation will be added in Phase 2.
 */
public interface DoctorDao extends GenericDao<Doctor, Integer> {

    /**
     * Finds doctors filtered by their medical department.
     *
     * @param department the department name (e.g. "Cardiology")
     * @return List of doctors in that department
     */
    List<Doctor> findByDepartment(String department);

    /**
     * Finds doctors filtered by specialization.
     *
     * @param specialization specialization name (e.g. "Pediatrics")
     * @return List of matching doctors
     */
    List<Doctor> findBySpecialization(String specialization);
}
