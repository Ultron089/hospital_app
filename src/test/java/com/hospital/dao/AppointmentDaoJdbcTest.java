package com.hospital.dao;

import com.hospital.dao.impl.AppointmentDaoJdbcImpl;
import com.hospital.dao.impl.DoctorDaoJdbcImpl;
import com.hospital.dao.impl.PatientDaoJdbcImpl;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.service.impl.AppointmentServiceImpl;
import com.hospital.service.impl.DoctorServiceImpl;
import com.hospital.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentDaoJdbcTest extends BaseDaoTest {

    private PatientDao patientDao;
    private DoctorDao doctorDao;
    private AppointmentDaoJdbcImpl appointmentDao;

    private Patient patient1;
    private Patient patient2;
    private Doctor doctor1;
    private Doctor doctor2;

    @BeforeEach
    void setupEntities() {
        patientDao = new PatientDaoJdbcImpl();
        doctorDao = new DoctorDaoJdbcImpl();
        appointmentDao = new AppointmentDaoJdbcImpl();

        patient1 = patientDao.save(new Patient(0, "Alice", "Smith", "a@test.com", "+1-555-1111",
                "123 St", LocalDate.of(1990, 1, 1), "A+", "None", "Contact"));
        patient2 = patientDao.save(new Patient(0, "Bob", "Jones", "b@test.com", "+1-555-2222",
                "456 St", LocalDate.of(1992, 2, 2), "B+", "None", "Contact"));

        doctor1 = doctorDao.save(new Doctor(0, "Evelyn", "Reed", "e@hosp.org", "+1-555-3333",
                "Suite 1", "Cardiology", "MD-NY-101", 150.0, "Cardiology"));
        doctor2 = doctorDao.save(new Doctor(0, "Marcus", "Vance", "m@hosp.org", "+1-555-4444",
                "Suite 2", "Pediatrics", "MD-NY-102", 120.0, "Pediatrics"));
    }

    @Test
    @DisplayName("Book appointment and verify full JOIN composition retrieval")
    void testSaveAndFindById() {
        LocalDateTime time = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Appointment apt = new Appointment(0, patient1, doctor1, time, "Cardiac Checkup");

        Appointment saved = appointmentDao.save(apt);
        assertTrue(saved.getId() > 0);

        Appointment found = appointmentDao.findById(saved.getId()).orElseThrow();
        assertEquals("Alice Smith", found.getPatient().getFullName());
        assertEquals("Evelyn Reed", found.getDoctor().getFullName());
        assertEquals(AppointmentStatus.SCHEDULED, found.getStatus());
        assertEquals("Cardiac Checkup", found.getReason());
    }

    @Test
    @DisplayName("Enforce active slot conflict prevention at the database constraint level")
    void testActiveSlotConflictPrevention() {
        LocalDateTime conflictTime = LocalDateTime.now().plusDays(5).withHour(14).withMinute(0).withSecond(0).withNano(0);

        // 1. Patient 1 books Doctor 1 at conflictTime -> Succeeded
        Appointment apt1 = new Appointment(0, patient1, doctor1, conflictTime, "First Consultation");
        appointmentDao.save(apt1);

        // Verify slot is unavailable via query
        assertFalse(appointmentDao.isSlotAvailable(doctor1.getId(), conflictTime));

        // 2. Patient 2 attempts to book Doctor 1 at EXACT same time -> MUST FAIL at DB constraint level!
        Appointment aptConflict = new Appointment(0, patient2, doctor1, conflictTime, "Conflicting Consultation");
        DataAccessException ex = assertThrows(DataAccessException.class, () -> appointmentDao.save(aptConflict));
        assertTrue(ex.getMessage().contains("already has an active appointment"), "Exception should mention active appointment collision");

        // 3. But Patient 2 CAN book Doctor 2 at that exact same time slot without collision!
        assertTrue(appointmentDao.isSlotAvailable(doctor2.getId(), conflictTime));
        Appointment aptDoc2 = new Appointment(0, patient2, doctor2, conflictTime, "Dr 2 Consultation");
        assertDoesNotThrow(() -> appointmentDao.save(aptDoc2));
    }

    @Test
    @DisplayName("Cancelling appointment preserves history and releases slot for rebooking")
    void testCancellationPreservesHistoryAndFreesSlot() {
        LocalDateTime slot = LocalDateTime.now().plusDays(7).withHour(11).withMinute(30).withSecond(0).withNano(0);

        // 1. Patient 1 books Doctor 1
        Appointment booked = appointmentDao.save(new Appointment(0, patient1, doctor1, slot, "Pre-surgery"));
        assertFalse(appointmentDao.isSlotAvailable(doctor1.getId(), slot));

        // 2. Cancel the appointment
        Appointment cancelled = appointmentDao.updateStatus(booked.getId(), AppointmentStatus.CANCELLED);
        assertEquals(AppointmentStatus.CANCELLED, cancelled.getStatus());

        // 3. Verify history is preserved: the record still exists in the database
        Appointment historyRecord = appointmentDao.findById(booked.getId()).orElseThrow();
        assertEquals(AppointmentStatus.CANCELLED, historyRecord.getStatus());
        assertEquals("Pre-surgery", historyRecord.getReason());

        // 4. Verify slot is now free again
        assertTrue(appointmentDao.isSlotAvailable(doctor1.getId(), slot));

        // 5. Patient 2 can now successfully book the released slot!
        Appointment rebooked = appointmentDao.save(new Appointment(0, patient2, doctor1, slot, "Replacement Consultation"));
        assertTrue(rebooked.getId() > 0);
        assertEquals(AppointmentStatus.SCHEDULED, rebooked.getStatus());
        assertEquals("Bob Jones", rebooked.getPatient().getFullName());
    }

    @Test
    @DisplayName("Query appointments by date, patient ID, and doctor ID")
    void testQueryFilters() {
        LocalDateTime time = LocalDateTime.now().plusDays(3).withHour(9).withMinute(0).withSecond(0).withNano(0);
        appointmentDao.save(new Appointment(0, patient1, doctor1, time, "Filter Test"));

        List<Appointment> byDate = appointmentDao.findByDate(time.toLocalDate());
        assertFalse(byDate.isEmpty());

        List<Appointment> byPatient = appointmentDao.findByPatientId(patient1.getId());
        assertEquals(1, byPatient.size());

        List<Appointment> byDoctor = appointmentDao.findByDoctorId(doctor1.getId());
        assertEquals(1, byDoctor.size());
    }

    @Test
    @DisplayName("Appointments stored in database are correctly grouped by doctor via AppointmentService")
    void testAppointmentsStoredInDatabaseGroupedByDoctor() {
        PatientService pService = new PatientServiceImpl(patientDao);
        DoctorService dService = new DoctorServiceImpl(doctorDao);
        AppointmentService apptService = new AppointmentServiceImpl(pService, dService, appointmentDao);

        LocalDateTime time1 = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime time2 = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime time3 = LocalDateTime.now().plusDays(4).withHour(11).withMinute(0).withSecond(0).withNano(0);

        appointmentDao.save(new Appointment(0, patient1, doctor1, time1, "Checkup Doc1 A"));
        appointmentDao.save(new Appointment(0, patient2, doctor1, time2, "Checkup Doc1 B"));
        appointmentDao.save(new Appointment(0, patient1, doctor2, time3, "Checkup Doc2 A"));

        Map<Integer, List<Appointment>> grouped = apptService.getAppointmentsGroupedByDoctor();
        assertNotNull(grouped, "Grouped map should not be null in JDBC mode");
        assertEquals(2, grouped.size(), "Should have entries for 2 distinct doctors");
        assertTrue(grouped.containsKey(doctor1.getId()));
        assertTrue(grouped.containsKey(doctor2.getId()));
        assertEquals(2, grouped.get(doctor1.getId()).size(), "Doctor 1 should have 2 appointments");
        assertEquals(1, grouped.get(doctor2.getId()).size(), "Doctor 2 should have 1 appointment");
    }

    @Test
    @DisplayName("Verify slot availability checking via AppointmentDao interface")
    void testSlotAvailabilityViaDaoInterface() {
        AppointmentDao daoInterface = appointmentDao;
        LocalDateTime slot = LocalDateTime.now().plusDays(6).withHour(15).withMinute(0).withSecond(0).withNano(0);

        assertTrue(daoInterface.isSlotAvailable(doctor1.getId(), slot));

        daoInterface.save(new Appointment(0, patient1, doctor1, slot, "Scheduled Booking"));
        assertFalse(daoInterface.isSlotAvailable(doctor1.getId(), slot));
    }
}
