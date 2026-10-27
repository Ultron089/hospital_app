package com.hospital.service;

import com.hospital.exception.HospitalException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.impl.AppointmentServiceImpl;
import com.hospital.service.impl.DoctorServiceImpl;
import com.hospital.service.impl.PatientServiceImpl;
import com.hospital.util.DbConnectionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HospitalServiceTest {

    private PatientService patientService;
    private DoctorService doctorService;
    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        patientService = new PatientServiceImpl();
        doctorService = new DoctorServiceImpl();
        appointmentService = new AppointmentServiceImpl(patientService, doctorService);
    }

    // ========================================================================
    // 1. PATIENT MANAGEMENT TESTS
    // ========================================================================

    @Test
    @DisplayName("Register valid patient and verify ID generation & Map lookup")
    void testRegisterValidPatient() throws HospitalException {
        Patient newPatient = new Patient(
                0, "Bruce", "Wayne", "bruce@wayne-enterprises.com", "+1-555-9999",
                "1007 Mountain Drive", LocalDate.of(1980, 2, 19), "O+", "Minor contusions", "Alfred (+1-555-8888)"
        );

        Patient registered = patientService.registerPatient(newPatient);
        assertTrue(registered.getId() > 100, "Generated ID should be positive");
        assertEquals("Bruce Wayne", registered.getFullName());

        // Verify retrieval by ID (Map lookup)
        Patient found = patientService.getPatientById(registered.getId());
        assertEquals("Bruce", found.getFirstName());
        assertEquals("bruce@wayne-enterprises.com", found.getEmail());

        // Verify Map contains patient
        assertTrue(patientService.getPatientMap().containsKey(registered.getId()));
    }

    @Test
    @DisplayName("Patient registration fails when required fields are missing")
    void testPatientValidationMissingFields() {
        // Blank first name
        Patient p1 = new Patient(0, "", "Wayne", "+1-555-1111");
        assertThrows(ValidationException.class, () -> patientService.registerPatient(p1));

        // Blank last name
        Patient p2 = new Patient(0, "Bruce", "   ", "+1-555-1111");
        assertThrows(ValidationException.class, () -> patientService.registerPatient(p2));

        // Blank phone
        Patient p3 = new Patient(0, "Bruce", "Wayne", "  ");
        assertThrows(ValidationException.class, () -> patientService.registerPatient(p3));
    }

    @Test
    @DisplayName("Patient registration fails on invalid email or phone formats")
    void testPatientValidationInvalidFormats() {
        Patient invalidEmailPatient = new Patient(
                0, "Bruce", "Wayne", "not-an-email", "+1-555-9999",
                "Gotham", LocalDate.of(1985, 1, 1), "A+", "None", "Alfred"
        );
        assertThrows(ValidationException.class, () -> patientService.registerPatient(invalidEmailPatient));

        Patient invalidPhonePatient = new Patient(
                0, "Bruce", "Wayne", "bruce@batman.com", "123", // too short
                "Gotham", LocalDate.of(1985, 1, 1), "A+", "None", "Alfred"
        );
        assertThrows(ValidationException.class, () -> patientService.registerPatient(invalidPhonePatient));

        // Future Date of Birth
        Patient futureDobPatient = new Patient(
                0, "Bruce", "Wayne", "bruce@batman.com", "+1-555-9999",
                "Gotham", LocalDate.now().plusDays(2), "A+", "None", "Alfred"
        );
        assertThrows(ValidationException.class, () -> patientService.registerPatient(futureDobPatient));
    }

    @Test
    @DisplayName("Search patients using lambda filtering")
    void testSearchPatients() {
        List<Patient> results = patientService.searchPatients("Alice");
        assertFalse(results.isEmpty());
        assertEquals("Alice Johnson", results.get(0).getFullName());

        List<Patient> bloodResults = patientService.searchPatients("B-");
        assertFalse(bloodResults.isEmpty());
        assertEquals("Sarah Williams", bloodResults.get(0).getFullName());
    }

    @Test
    @DisplayName("Reject patient registration when phone number is already registered")
    void testRejectDuplicatePatientPhoneNumber() {
        // Alice Johnson's phone in sample data is +1-555-0101
        Patient duplicatePhonePatient = new Patient(
                0, "Duplicate", "User", "dup@example.com", "+1-555-0101",
                "Address", LocalDate.of(1990, 1, 1), "O+", "None", "Contact"
        );

        ValidationException ex = assertThrows(ValidationException.class, () ->
                patientService.registerPatient(duplicatePhonePatient));
        assertTrue(ex.getMessage().contains("already registered"));
        assertEquals("phoneNumber", ex.getFieldName());
    }

    @Test
    @DisplayName("Patient registration assigns unique sequential IDs")
    void testPatientUniqueIdGeneration() throws HospitalException {
        Patient p1 = new Patient(0, "Patient", "One", "p1@example.com", "+1-555-9001",
                "Street 1", LocalDate.of(1990, 1, 1), "A+", "None", "Contact");
        Patient p2 = new Patient(0, "Patient", "Two", "p2@example.com", "+1-555-9002",
                "Street 2", LocalDate.of(1991, 2, 2), "B+", "None", "Contact");

        Patient reg1 = patientService.registerPatient(p1);
        Patient reg2 = patientService.registerPatient(p2);

        assertNotEquals(reg1.getId(), reg2.getId());
        assertEquals(reg1.getId() + 1, reg2.getId());
    }

    // ========================================================================
    // 2. DOCTOR MANAGEMENT TESTS
    // ========================================================================

    @Test
    @DisplayName("Register valid doctor and verify unique specializations Set")
    void testRegisterDoctorAndSpecializationSet() throws HospitalException {
        Doctor doc = new Doctor(
                0, "Gregory", "House", "house@princeton-plainsboro.org", "+1-555-4321",
                "Diag Office", "Diagnostic Medicine", "MD-NJ-001", 250.0, "Diagnostics"
        );

        Doctor registered = doctorService.registerDoctor(doc);
        assertTrue(registered.getId() > 200);

        // Verify Set contains the new specialization
        Set<String> specializations = doctorService.getAllSpecializations();
        assertTrue(specializations.contains("Diagnostic Medicine"));
        assertTrue(specializations.contains("Cardiology")); // from initial seed
        assertTrue(specializations.contains("Pediatrics"));
    }

    @Test
    @DisplayName("Doctor registration fails on invalid fee or missing details")
    void testDoctorValidationFailures() {
        Doctor missingSpec = new Doctor(0, "John", "Watson", "");
        assertThrows(ValidationException.class, () -> doctorService.registerDoctor(missingSpec));

        // Empty license
        Doctor emptyLicenseDoc = new Doctor(
                0, "John", "Watson", "watson@baker.org", "+1-555-0987",
                "Baker St", "General Medicine", "", 100.0, "General OPD"
        );
        assertThrows(ValidationException.class, () -> doctorService.registerDoctor(emptyLicenseDoc));

        // Zero fee
        Doctor zeroFeeDoc = new Doctor(
                0, "John", "Watson", "watson@baker.org", "+1-555-0987",
                "Baker St", "General Medicine", "MD-TEST-003", 0.0, "General OPD"
        );
        assertThrows(ValidationException.class, () -> doctorService.registerDoctor(zeroFeeDoc));

        // Invalid phone
        Doctor invalidPhoneDoc = new Doctor(
                0, "John", "Watson", "watson@baker.org", "123",
                "Baker St", "General Medicine", "MD-TEST-004", 100.0, "General OPD"
        );
        assertThrows(ValidationException.class, () -> doctorService.registerDoctor(invalidPhoneDoc));

        // Invalid email
        Doctor invalidEmailDoc = new Doctor(
                0, "John", "Watson", "invalid-email-address", "+1-555-0987",
                "Baker St", "General Medicine", "MD-TEST-005", 100.0, "General OPD"
        );
        assertThrows(ValidationException.class, () -> doctorService.registerDoctor(invalidEmailDoc));
    }

    @Test
    @DisplayName("Reject doctor registration when medical license number is already registered")
    void testRejectDuplicateDoctorLicenseNumber() {
        // Evelyn Reed's license in sample data is MD-NY-84920
        Doctor duplicateLicenseDoc = new Doctor(
                0, "Stephen", "Strange", "strange@hospital.org", "+1-555-7777",
                "Sanctum", "Neurology", "MD-NY-84920", 300.0, "Neurosciences"
        );

        ValidationException ex = assertThrows(ValidationException.class, () ->
                doctorService.registerDoctor(duplicateLicenseDoc));
        assertTrue(ex.getMessage().contains("already registered"));
        assertEquals("licenseNumber", ex.getFieldName());
    }

    @Test
    @DisplayName("Doctor registration assigns unique sequential IDs")
    void testDoctorUniqueIdGeneration() throws HospitalException {
        Doctor d1 = new Doctor(0, "Doctor", "Alpha", "alpha@hospital.org", "+1-555-8001",
                "Suite A", "Cardiology", "MD-TEST-001", 150.0, "Cardiology");
        Doctor d2 = new Doctor(0, "Doctor", "Beta", "beta@hospital.org", "+1-555-8002",
                "Suite B", "Neurology", "MD-TEST-002", 180.0, "Neurology");

        Doctor reg1 = doctorService.registerDoctor(d1);
        Doctor reg2 = doctorService.registerDoctor(d2);

        assertNotEquals(reg1.getId(), reg2.getId());
        assertEquals(reg1.getId() + 1, reg2.getId());
    }

    @Test
    @DisplayName("Search doctors by specialization and department")
    void testSearchDoctors() {
        List<Doctor> cardioDocs = doctorService.searchDoctors("Cardiology");
        assertFalse(cardioDocs.isEmpty());
        assertEquals("Evelyn Reed", cardioDocs.get(0).getFullName());

        List<Doctor> pedsDocs = doctorService.getDoctorsByDepartment("Child & Adolescent Health");
        assertEquals(1, pedsDocs.size());
    }

    // ========================================================================
    // 3. APPOINTMENT MANAGEMENT TESTS
    // ========================================================================

    @Test
    @DisplayName("Book valid appointment successfully")
    void testBookValidAppointment() throws HospitalException {
        LocalDateTime futureSlot = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Appointment apt = appointmentService.bookAppointment(101, 201, futureSlot, "Routine Follow-up");

        assertNotNull(apt);
        assertEquals(101, apt.getPatient().getId());
        assertEquals(201, apt.getDoctor().getId());
        assertEquals(AppointmentStatus.SCHEDULED, apt.getStatus());
        assertEquals("Routine Follow-up", apt.getReason());
    }

    @Test
    @DisplayName("Reject appointment scheduled in the past")
    void testRejectPastAppointment() {
        LocalDateTime pastSlot = LocalDateTime.now().minusHours(2);
        assertThrows(ValidationException.class, () ->
                appointmentService.bookAppointment(101, 201, pastSlot, "Past Consultation"));
    }

    @Test
    @DisplayName("Prevent booking the same doctor date/time slot twice (Collision Prevention)")
    void testPreventDuplicateSlotBooking() throws HospitalException {
        LocalDateTime conflictSlot = LocalDateTime.now().plusDays(12).withHour(15).withMinute(0).withSecond(0).withNano(0);

        // Patient 1 books Dr. 201
        appointmentService.bookAppointment(101, 201, conflictSlot, "First Booking");

        // Verify slot is no longer available for Dr. 201
        assertFalse(appointmentService.isDoctorSlotAvailable(201, conflictSlot));

        // Patient 2 attempts to book Dr. 201 at the EXACT same slot -> MUST FAIL
        ValidationException ex = assertThrows(ValidationException.class, () ->
                appointmentService.bookAppointment(102, 201, conflictSlot, "Conflicting Booking"));

        assertTrue(ex.getMessage().contains("already has an active appointment"));

        // However, Patient 2 CAN book Dr. 202 at the same time slot!
        assertTrue(appointmentService.isDoctorSlotAvailable(202, conflictSlot));
        assertDoesNotThrow(() ->
                appointmentService.bookAppointment(102, 202, conflictSlot, "Dr 202 Booking"));
    }

    @Test
    @DisplayName("Cancel appointment and verify slot becomes available again")
    void testCancelAppointmentAndFreeSlot() throws HospitalException {
        LocalDateTime slot = LocalDateTime.now().plusDays(14).withHour(9).withMinute(0).withSecond(0).withNano(0);

        Appointment booked = appointmentService.bookAppointment(101, 203, slot, "Pre-surgery Consultation");
        assertEquals(AppointmentStatus.SCHEDULED, booked.getStatus());
        assertFalse(appointmentService.isDoctorSlotAvailable(203, slot));

        // Cancel the appointment
        Appointment cancelled = appointmentService.cancelAppointment(booked.getId());
        assertEquals(AppointmentStatus.CANCELLED, cancelled.getStatus());

        // Now the slot MUST be available again!
        assertTrue(appointmentService.isDoctorSlotAvailable(203, slot));

        // Another patient can now book this slot
        assertDoesNotThrow(() ->
                appointmentService.bookAppointment(104, 203, slot, "Replacement Booking"));
    }

    @Test
    @DisplayName("Group appointments by doctor using Map<Integer, List<Appointment>>")
    void testAppointmentsGroupedByDoctorMap() {
        Map<Integer, List<Appointment>> grouped = appointmentService.getAppointmentsGroupedByDoctor();
        assertNotNull(grouped);
        assertFalse(grouped.isEmpty());

        // Doctors 201, 202, 203, 204 from sample data should each have appointments
        assertTrue(grouped.containsKey(201));
        assertFalse(grouped.get(201).isEmpty());
    }

    @Test
    @DisplayName("MockDataService fails clearly when database is configured but unreachable")
    void testMockDataServiceFailsWhenDbUnreachable() {
        // Configure an invalid unreachable database port
        DbConnectionManager.configure("jdbc:mysql://localhost:9999/nonexistent_db", "user", "pass", "com.mysql.cj.jdbc.Driver");
        try {
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> new MockDataService());
            assertTrue(ex.getMessage().contains("Database configuration detected in db.properties, but connection failed"),
                    "Should fail with clear database connection error and not fall back to in-memory");
        } finally {
            DbConnectionManager.reset();
        }
    }
}
