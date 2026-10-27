package com.hospital.model;

import com.hospital.exception.ValidationException;
import com.hospital.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying Phase 1 Object-Oriented Programming (OOP) requirements:
 * Encapsulation, Inheritance, Abstraction, Polymorphism, and Composition.
 */
class ModelTest {

    @Test
    @DisplayName("Verify Patient Inheritance and Polymorphism with Person")
    void testPatientInheritanceAndPolymorphism() {
        // Patient "IS-A" Person (Polymorphic assignment)
        Person patientAsPerson = new Patient(
                1, "John", "Doe", "john.doe@example.com", "+1-555-1234",
                "123 Main St", LocalDate.of(1990, 5, 10), "O+",
                "No known allergies", "Jane Doe (+1-555-5678)"
        );

        // Test inherited methods
        assertEquals("John Doe", patientAsPerson.getFullName());
        assertEquals(1, patientAsPerson.getId());
        assertEquals("john.doe@example.com", patientAsPerson.getEmail());

        // Test polymorphic method overriding
        assertEquals("Patient", patientAsPerson.getRole());
        assertTrue(patientAsPerson.getDisplayInfo().contains("Blood: O+"));

        // Test subclass-specific method via downcasting
        Patient patient = (Patient) patientAsPerson;
        assertEquals("O+", patient.getBloodGroup());
        assertTrue(patient.getAge() >= 30);
    }

    @Test
    @DisplayName("Verify Doctor Inheritance, Role Overriding, and Fee Encapsulation")
    void testDoctorInheritanceAndEncapsulation() {
        Doctor doctor = new Doctor(
                2, "Sarah", "Connor", "sarah.connor@hospital.org", "+1-555-9876",
                "Hospital Suite 200", "Cardiology", "MD-12345", 150.0, "Cardiology"
        );

        // Verification of Role & Inheritance
        assertEquals("Doctor", doctor.getRole());
        assertEquals("Sarah Connor", doctor.getFullName());
        assertEquals("Cardiology", doctor.getSpecialization());

        // Verification of Fee Validation (Encapsulation protects against invalid state)
        doctor.setConsultationFee(-50.0);
        assertEquals(0.0, doctor.getConsultationFee(), "Negative fee should be clamped to 0.0");

        doctor.setConsultationFee(200.0);
        assertEquals(200.0, doctor.getConsultationFee());
        assertTrue(doctor.getDisplayInfo().contains("$200.00"));
    }

    @Test
    @DisplayName("Verify Appointment Composition (HAS-A relationship)")
    void testAppointmentComposition() {
        Patient p = new Patient(10, "Alice", "Smith", "+1-555-1111");
        Doctor d = new Doctor(20, "Robert", "Lang", "Neurology");
        LocalDateTime apptTime = LocalDateTime.of(2026, 11, 15, 10, 30);

        Appointment apt = new Appointment(100, p, d, apptTime, "Headache Consultation");

        assertEquals(100, apt.getId());
        assertEquals("Alice Smith", apt.getPatient().getFullName());
        assertEquals("Robert Lang", apt.getDoctor().getFullName());
        assertEquals(AppointmentStatus.SCHEDULED, apt.getStatus());
        assertEquals("Headache Consultation", apt.getReason());

        // Update Status
        apt.setStatus(AppointmentStatus.CONFIRMED);
        assertEquals(AppointmentStatus.CONFIRMED, apt.getStatus());
        assertEquals("Confirmed", apt.getStatus().getDisplayName());
    }

    @Test
    @DisplayName("Verify Validation Utilities and Custom Exceptions")
    void testValidationUtility() {
        assertThrows(ValidationException.class, () ->
                ValidationUtil.requireNonEmpty("", "Patient Name"));

        assertThrows(ValidationException.class, () ->
                ValidationUtil.validateEmail("invalid-email-address"));

        assertDoesNotThrow(() ->
                ValidationUtil.validateEmail("valid.doctor@hospital.org"));
    }
}
