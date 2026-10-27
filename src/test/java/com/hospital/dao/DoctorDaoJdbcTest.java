package com.hospital.dao;

import com.hospital.dao.impl.DoctorDaoJdbcImpl;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Doctor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DoctorDaoJdbcTest extends BaseDaoTest {

    private DoctorDao doctorDao;

    @BeforeEach
    void initDao() {
        doctorDao = new DoctorDaoJdbcImpl();
    }

    @Test
    @DisplayName("Save doctor with foreign key to specialization and verify auto-generated ID")
    void testSaveAndFindById() {
        Doctor doc = new Doctor(
                0, "Gregory", "House", "house@hospital.org", "+1-555-4321",
                "Diagnostics Suite", "Cardiology", "MD-NJ-001", 250.0, "Cardiovascular Diseases"
        );

        Doctor saved = doctorDao.save(doc);
        assertTrue(saved.getId() > 0);

        Optional<Doctor> found = doctorDao.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Gregory House", found.get().getFullName());
        assertEquals("MD-NJ-001", found.get().getLicenseNumber());
        assertEquals("Cardiology", found.get().getSpecialization());
    }

    @Test
    @DisplayName("Reject duplicate doctor license number at database constraint level")
    void testRejectDuplicateLicenseNumber() {
        Doctor d1 = new Doctor(0, "Doctor", "One", "d1@hospital.org", "+1-555-1111",
                "Suite 1", "Cardiology", "MD-DUP-999", 150.0, "Cardiology");
        doctorDao.save(d1);

        Doctor d2 = new Doctor(0, "Doctor", "Two", "d2@hospital.org", "+1-555-2222",
                "Suite 2", "Neurology", "MD-DUP-999", 200.0, "Neurology");

        assertThrows(DataAccessException.class, () -> doctorDao.save(d2));
    }

    @Test
    @DisplayName("Query doctors by department and specialization")
    void testFindByDepartmentAndSpecialization() {
        doctorDao.save(new Doctor(0, "James", "Wilson", "wilson@hospital.org", "+1-555-3333",
                "Suite 3", "Orthopedics", "MD-ORTHO-1", 180.0, "Bone & Joint"));

        List<Doctor> byDept = doctorDao.findByDepartment("Bone & Joint");
        assertEquals(1, byDept.size());
        assertEquals("James Wilson", byDept.get(0).getFullName());

        List<Doctor> bySpec = doctorDao.findBySpecialization("Orthopedics");
        assertEquals(1, bySpec.size());
    }

    @Test
    @DisplayName("Retrieve all unique specializations from database")
    void testGetAllSpecializations() {
        DoctorDaoJdbcImpl impl = (DoctorDaoJdbcImpl) doctorDao;
        Set<String> specs = impl.getAllSpecializations();

        assertNotNull(specs);
        assertTrue(specs.contains("Cardiology"));
        assertTrue(specs.contains("Pediatrics"));
        assertTrue(specs.contains("Neurology"));
    }
}
