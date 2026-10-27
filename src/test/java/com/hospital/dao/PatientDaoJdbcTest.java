package com.hospital.dao;

import com.hospital.dao.impl.PatientDaoJdbcImpl;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PatientDaoJdbcTest extends BaseDaoTest {

    private PatientDao patientDao;

    @BeforeEach
    void initDao() {
        patientDao = new PatientDaoJdbcImpl();
    }

    @Test
    @DisplayName("Save patient and verify auto-generated ID retrieval")
    void testSaveAndFindById() {
        Patient p = new Patient(
                0, "Bruce", "Wayne", "bruce@wayne.com", "+1-555-9090",
                "1007 Mountain Dr", LocalDate.of(1982, 3, 19), "O+", "Minor injuries", "Alfred"
        );

        Patient saved = patientDao.save(p);
        assertTrue(saved.getId() > 0, "Generated ID should be positive");

        Optional<Patient> found = patientDao.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Bruce Wayne", found.get().getFullName());
        assertEquals("+1-555-9090", found.get().getPhoneNumber());
        assertEquals("O+", found.get().getBloodGroup());
    }

    @Test
    @DisplayName("Reject duplicate patient phone number at database constraint level")
    void testRejectDuplicatePhoneNumber() {
        Patient p1 = new Patient(0, "User", "One", "u1@test.com", "+1-555-1234",
                "Address", LocalDate.of(1990, 1, 1), "A+", "None", "Contact");
        patientDao.save(p1);

        Patient p2 = new Patient(0, "User", "Two", "u2@test.com", "+1-555-1234",
                "Address", LocalDate.of(1992, 2, 2), "B+", "None", "Contact");

        assertThrows(DataAccessException.class, () -> patientDao.save(p2));
    }

    @Test
    @DisplayName("Update patient and verify updated fields")
    void testUpdatePatient() {
        Patient p = patientDao.save(new Patient(
                0, "Clark", "Kent", "clark@dailyplanet.com", "+1-555-3333",
                "Metropolis", LocalDate.of(1985, 6, 18), "Unknown", "None", "Lois"
        ));

        p.setPhoneNumber("+1-555-4444");
        p.setMedicalHistory("Kryptonite sensitivity");
        patientDao.update(p);

        Patient updated = patientDao.findById(p.getId()).orElseThrow();
        assertEquals("+1-555-4444", updated.getPhoneNumber());
        assertEquals("Kryptonite sensitivity", updated.getMedicalHistory());
    }

    @Test
    @DisplayName("Search patients by name or phone query")
    void testSearchByName() {
        patientDao.save(new Patient(0, "Diana", "Prince", "diana@themyscira.gov", "+1-555-7777",
                "Gateway City", LocalDate.of(1980, 5, 22), "AB+", "Immortal", "Hippolyta"));

        List<Patient> byFirst = patientDao.searchByName("Diana");
        assertFalse(byFirst.isEmpty());
        assertEquals("Diana Prince", byFirst.get(0).getFullName());

        List<Patient> byPhone = patientDao.searchByName("555-7777");
        assertFalse(byPhone.isEmpty());
        assertEquals("Diana Prince", byPhone.get(0).getFullName());
    }

    @Test
    @DisplayName("Find patient by phone number")
    void testFindByPhoneNumber() {
        patientDao.save(new Patient(0, "Barry", "Allen", "barry@ccpd.gov", "+1-555-8888",
                "Central City", LocalDate.of(1993, 11, 14), "O-", "Lightning strike", "Iris"));

        Patient found = patientDao.findByPhoneNumber("+1-555-8888");
        assertNotNull(found);
        assertEquals("Barry Allen", found.getFullName());

        assertNull(patientDao.findByPhoneNumber("+1-555-0000"));
    }
}
