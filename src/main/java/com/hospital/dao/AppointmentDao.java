package com.hospital.dao;

import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object contract for Appointment entities.
 * Inherits standard CRUD methods from GenericDao and declares appointment-specific queries.
 * Concrete JDBC implementation will be added in Phase 2.
 */
public interface AppointmentDao extends GenericDao<Appointment, Integer> {

    /**
     * Finds appointments scheduled for a specific date.
     *
     * @param date Date of appointments
     * @return List of appointments on that day
     */
    List<Appointment> findByDate(LocalDate date);

    /**
     * Finds all appointments associated with a patient ID.
     *
     * @param patientId ID of the patient
     * @return List of appointments
     */
    List<Appointment> findByPatientId(int patientId);

    /**
     * Finds all appointments associated with a doctor ID.
     *
     * @param doctorId ID of the doctor
     * @return List of appointments
     */
    List<Appointment> findByDoctorId(int doctorId);

    /**
     * Finds appointments by current status.
     *
     * @param status appointment status
     * @return List of matching appointments
     */
    List<Appointment> findByStatus(AppointmentStatus status);

    /**
     * Updates only the appointment status.
     *
     * @param appointmentId ID of the appointment to update
     * @param newStatus new status value
     * @return updated Appointment instance
     */
    Appointment updateStatus(int appointmentId, AppointmentStatus newStatus);

    /**
     * Checks if a doctor has an active (non-cancelled) appointment at the specified date and time.
     *
     * @param doctorId ID of the doctor
     * @param dateTime appointment date and time
     * @return true if the slot is free, false otherwise
     */
    boolean isSlotAvailable(int doctorId, LocalDateTime dateTime);
}
