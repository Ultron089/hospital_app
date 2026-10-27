package com.hospital.service;

import com.hospital.exception.HospitalException;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service contract for Appointment scheduling and management.
 */
public interface AppointmentService {

    /**
     * Schedules a new appointment between a patient and a doctor.
     *
     * @param patientId patient ID
     * @param doctorId  doctor ID
     * @param dateTime  appointment date/time
     * @param reason    reason for appointment
     * @return created Appointment
     * @throws HospitalException if validation or scheduling conflicts occur
     */
    Appointment bookAppointment(int patientId, int doctorId, LocalDateTime dateTime, String reason)
            throws HospitalException;

    /**
     * Retrieves all appointments.
     *
     * @return list of appointments
     */
    List<Appointment> getAllAppointments();

    /**
     * Retrieves appointments scheduled for a specific calendar date.
     *
     * @param date date to query
     * @return list of appointments
     */
    List<Appointment> getAppointmentsByDate(LocalDate date);

    /**
     * Updates the status of an appointment (e.g. Completed, Cancelled).
     *
     * @param appointmentId appointment ID
     * @param newStatus     new status enum
     * @return updated Appointment
     * @throws HospitalException if appointment not found
     */
    Appointment updateAppointmentStatus(int appointmentId, AppointmentStatus newStatus)
            throws HospitalException;

    /**
     * Cancels an appointment by updating its status to CANCELLED.
     *
     * @param appointmentId appointment ID
     * @return cancelled Appointment
     * @throws HospitalException if appointment not found
     */
    Appointment cancelAppointment(int appointmentId) throws HospitalException;

    /**
     * Checks if a doctor is available at the specified date and time.
     * Prevents duplicate active booking slots for the same doctor.
     *
     * @param doctorId doctor ID
     * @param dateTime appointment date and time
     * @return true if the slot is free, false if already booked
     */
    boolean isDoctorSlotAvailable(int doctorId, LocalDateTime dateTime);

    /**
     * Returns an unmodifiable Map of appointments indexed by their ID.
     * Demonstrates the Java Collections Map concept.
     *
     * @return Map of ID to Appointment
     */
    java.util.Map<Integer, Appointment> getAppointmentMap();

    /**
     * Groups all appointments by doctor ID.
     * Demonstrates Map<Integer, List<Appointment>> and stream groupingBy collectors.
     *
     * @return Map of doctorId -> List of appointments
     */
    java.util.Map<Integer, List<Appointment>> getAppointmentsGroupedByDoctor();
}
