package com.hospital.service;

import com.hospital.exception.HospitalException;
import com.hospital.model.Doctor;

import java.util.List;

/**
 * Service contract for Doctor business operations.
 */
public interface DoctorService {

    /**
     * Registers a new doctor into the hospital roster.
     *
     * @param doctor doctor entity
     * @return registered doctor
     * @throws HospitalException if validation fails
     */
    Doctor registerDoctor(Doctor doctor) throws HospitalException;

    /**
     * Retrieves all doctors in the hospital.
     *
     * @return list of doctors
     */
    List<Doctor> getAllDoctors();

    /**
     * Finds a doctor by ID.
     *
     * @param id doctor ID
     * @return Doctor entity
     * @throws HospitalException if doctor not found
     */
    Doctor getDoctorById(int id) throws HospitalException;

    /**
     * Retrieves doctors belonging to a specific department.
     *
     * @param department department name
     * @return list of doctors
     */
    List<Doctor> getDoctorsByDepartment(String department);

    /**
     * Searches doctors by name, department, or specialization.
     * Demonstrates lambda expression filtering.
     *
     * @param query search keyword
     * @return list of matching doctors
     */
    List<Doctor> searchDoctors(String query);

    /**
     * Retrieves the distinct set of all available medical specializations.
     * Demonstrates the Java Collections Set concept.
     *
     * @return Set of unique specialization names
     */
    java.util.Set<String> getAllSpecializations();

    /**
     * Returns an unmodifiable Map of doctors indexed by their ID.
     * Demonstrates the Java Collections Map concept for O(1) lookups.
     *
     * @return Map of ID to Doctor
     */
    java.util.Map<Integer, Doctor> getDoctorMap();
}
