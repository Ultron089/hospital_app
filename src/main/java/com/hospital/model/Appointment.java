package com.hospital.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * ============================================================================
 * JAVA OOP CONCEPTS DEMONSTRATED HERE:
 * ============================================================================
 * 1. COMPOSITION / AGGREGATION ("HAS-A" RELATIONSHIP):
 *    - An Appointment is NOT a Person. Instead, it HAS-A `Patient` and HAS-A `Doctor`.
 *    - In OOP, Composition/Aggregation is preferred over inheritance when models
 *      represent associations between different domain entities.
 *
 * 2. ENCAPSULATION:
 *    - All appointment attributes are private.
 *    - Controlled state transitions (e.g. status changes).
 *
 * 3. INTERFACES:
 *    - Implements `Identifiable` and `Displayable`.
 * ============================================================================
 */
public class Appointment implements Identifiable, Displayable {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ------------------------------------------------------------------------
    // ENCAPSULATED FIELDS
    // ------------------------------------------------------------------------
    private int id;
    private Patient patient;
    private Doctor doctor;
    private LocalDateTime appointmentDateTime;
    private AppointmentStatus status;
    private String reason;
    private String notes;

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    /**
     * Default No-Arg Constructor.
     */
    public Appointment() {
        this.id = 0;
        this.patient = new Patient();
        this.doctor = new Doctor();
        this.appointmentDateTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0);
        this.status = AppointmentStatus.SCHEDULED;
        this.reason = "Routine Checkup";
        this.notes = "";
    }

    /**
     * Parameterized Constructor.
     *
     * @param id                  Appointment ID
     * @param patient             Patient who has the appointment
     * @param doctor              Doctor attending the appointment
     * @param appointmentDateTime Date and time scheduled
     * @param reason              Reason for consultation
     */
    public Appointment(int id, Patient patient, Doctor doctor, LocalDateTime appointmentDateTime, String reason) {
        this(id, patient, doctor, appointmentDateTime, AppointmentStatus.SCHEDULED, reason, "");
    }

    /**
     * Full Parameterized Constructor.
     */
    public Appointment(int id, Patient patient, Doctor doctor, LocalDateTime appointmentDateTime,
                       AppointmentStatus status, String reason, String notes) {
        this.id = id;
        this.patient = (patient != null) ? patient : new Patient();
        this.doctor = (doctor != null) ? doctor : new Doctor();
        this.appointmentDateTime = (appointmentDateTime != null) ? appointmentDateTime : LocalDateTime.now();
        this.status = (status != null) ? status : AppointmentStatus.SCHEDULED;
        this.reason = (reason != null) ? reason.trim() : "General Consultation";
        this.notes = (notes != null) ? notes.trim() : "";
    }

    // ------------------------------------------------------------------------
    // INTERFACE IMPLEMENTATIONS & METHOD OVERRIDING
    // ------------------------------------------------------------------------

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getDisplayInfo() {
        String patientName = (patient != null) ? patient.getFullName() : "N/A";
        String doctorName = (doctor != null) ? doctor.getFullName() : "N/A";
        String timeStr = (appointmentDateTime != null) ? appointmentDateTime.format(FORMATTER) : "TBD";
        return String.format("[Apt #%d] %s with Dr. %s on %s [%s] - %s",
                id, patientName, doctorName, timeStr, status.getDisplayName(), reason);
    }

    // ------------------------------------------------------------------------
    // GETTERS AND SETTERS (ENCAPSULATION)
    // ------------------------------------------------------------------------

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = (patient != null) ? patient : new Patient();
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = (doctor != null) ? doctor : new Doctor();
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = (appointmentDateTime != null) ? appointmentDateTime : LocalDateTime.now();
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = (status != null) ? status : AppointmentStatus.SCHEDULED;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = (reason != null) ? reason.trim() : "";
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = (notes != null) ? notes.trim() : "";
    }

    /**
     * Helper to get formatted date string for tables and UI cards.
     */
    public String getFormattedDateTime() {
        return (appointmentDateTime != null) ? appointmentDateTime.format(FORMATTER) : "N/A";
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patient=" + (patient != null ? patient.getFullName() : "null") +
                ", doctor=" + (doctor != null ? doctor.getFullName() : "null") +
                ", dateTime=" + getFormattedDateTime() +
                ", status=" + status +
                ", reason='" + reason + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
