package com.hospital.service.impl;

import com.hospital.dao.DoctorDao;
import com.hospital.dao.impl.DoctorDaoJdbcImpl;
import com.hospital.exception.DataAccessException;
import com.hospital.exception.HospitalException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Doctor;
import com.hospital.service.DoctorService;
import com.hospital.util.ValidationUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Concrete implementation of DoctorService.
 * Supports both Database persistence (via DoctorDao) and In-Memory fallback.
 */
public class DoctorServiceImpl implements DoctorService {

    private final DoctorDao doctorDao;
    private final Map<Integer, Doctor> doctorMap = new ConcurrentHashMap<>();

    public DoctorServiceImpl() {
        this(null);
    }

    public DoctorServiceImpl(DoctorDao doctorDao) {
        this.doctorDao = doctorDao;
        if (doctorDao == null) {
            initSampleDoctors();
        }
    }

    private void initSampleDoctors() {
        addSample(new Doctor(201, "Evelyn", "Reed", "evelyn.reed@hospital.org", "+1-555-0201",
                "Hospital Suites #301", "Cardiology", "MD-NY-84920", 150.00, "Cardiovascular Diseases"));
        addSample(new Doctor(202, "Marcus", "Vance", "m.vance@hospital.org", "+1-555-0202",
                "Hospital Suites #204", "Pediatrics", "MD-NY-77431", 110.00, "Child & Adolescent Health"));
        addSample(new Doctor(203, "Elena", "Rostova", "e.rostova@hospital.org", "+1-555-0203",
                "Hospital Suites #405", "Neurology", "MD-NY-91823", 175.00, "Neurosciences"));
        addSample(new Doctor(204, "James", "Wilson", "j.wilson@hospital.org", "+1-555-0204",
                "Hospital Suites #112", "Orthopedics", "MD-NY-63201", 140.00, "Bone & Joint Surgery"));
    }

    private void addSample(Doctor d) {
        doctorMap.put(d.getId(), d);
    }

    @Override
    public Doctor registerDoctor(Doctor doctor) throws HospitalException {
        if (doctor == null) {
            throw new HospitalException("Doctor entity cannot be null.");
        }

        // Validate business constraints
        ValidationUtil.requireNonEmpty(doctor.getFirstName(), "First Name");
        ValidationUtil.requireNonEmpty(doctor.getLastName(), "Last Name");
        ValidationUtil.requireNonEmpty(doctor.getLicenseNumber(), "Medical License Number");
        ValidationUtil.requireNonEmpty(doctor.getSpecialization(), "Specialization");
        ValidationUtil.requireNonEmpty(doctor.getDepartment(), "Department");
        if (doctor.getConsultationFee() <= 0.0) {
            throw new ValidationException("consultationFee", "Consultation fee must be greater than zero.");
        }
        ValidationUtil.validatePhoneNumber(doctor.getPhoneNumber());
        ValidationUtil.validateEmail(doctor.getEmail());

        String license = doctor.getLicenseNumber().trim();

        if (doctorDao != null) {
            if (doctorDao instanceof DoctorDaoJdbcImpl impl) {
                Doctor existing = impl.findByLicenseNumber(license);
                if (existing != null) {
                    throw new ValidationException("licenseNumber",
                            "A doctor with medical license number '" + license + "' is already registered.");
                }
            }

            try {
                return doctorDao.save(doctor);
            } catch (DataAccessException e) {
                if (e.getMessage() != null && e.getMessage().contains("already registered")) {
                    throw new ValidationException("licenseNumber", e.getMessage());
                }
                throw new HospitalException(e.getMessage(), e);
            }
        }

        // In-Memory Fallback
        boolean licenseExists = doctorMap.values().stream()
                .anyMatch(d -> d.getLicenseNumber() != null && d.getLicenseNumber().equalsIgnoreCase(license));
        if (licenseExists) {
            throw new ValidationException("licenseNumber",
                    "A doctor with medical license number '" + license + "' is already registered.");
        }

        int nextId = doctorMap.keySet().stream().max(Integer::compareTo).orElse(200) + 1;
        doctor.setId(nextId);
        doctorMap.put(nextId, doctor);
        return doctor;
    }

    @Override
    public List<Doctor> getAllDoctors() {
        if (doctorDao != null) {
            return doctorDao.findAll();
        }
        return new ArrayList<>(doctorMap.values());
    }

    @Override
    public Doctor getDoctorById(int id) throws HospitalException {
        if (doctorDao != null) {
            return doctorDao.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        }
        Doctor doctor = doctorMap.get(id);
        if (doctor == null) {
            throw new ResourceNotFoundException("Doctor", id);
        }
        return doctor;
    }

    @Override
    public List<Doctor> getDoctorsByDepartment(String department) {
        if (doctorDao != null) {
            return doctorDao.findByDepartment(department);
        }
        if (department == null || department.trim().isEmpty()) {
            return getAllDoctors();
        }
        return doctorMap.values().stream()
                .filter(d -> d.getDepartment().equalsIgnoreCase(department.trim()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Doctor> searchDoctors(String query) {
        if (doctorDao != null) {
            if (doctorDao instanceof DoctorDaoJdbcImpl impl) {
                return impl.searchDoctors(query);
            }
            if (query == null || query.trim().isEmpty()) {
                return doctorDao.findAll();
            }
            String q = query.trim().toLowerCase();
            return doctorDao.findAll().stream()
                    .filter(d -> d.getFullName().toLowerCase().contains(q)
                            || d.getSpecialization().toLowerCase().contains(q)
                            || d.getDepartment().toLowerCase().contains(q)
                            || d.getLicenseNumber().toLowerCase().contains(q)
                            || String.valueOf(d.getId()).contains(q))
                    .collect(Collectors.toList());
        }

        if (query == null || query.trim().isEmpty()) {
            return getAllDoctors();
        }
        String q = query.trim().toLowerCase();

        return doctorMap.values().stream()
                .filter(d -> d.getFullName().toLowerCase().contains(q)
                        || d.getSpecialization().toLowerCase().contains(q)
                        || d.getDepartment().toLowerCase().contains(q)
                        || d.getLicenseNumber().toLowerCase().contains(q)
                        || String.valueOf(d.getId()).contains(q))
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getAllSpecializations() {
        if (doctorDao != null) {
            if (doctorDao instanceof DoctorDaoJdbcImpl impl) {
                return impl.getAllSpecializations();
            }
            return doctorDao.findAll().stream()
                    .map(Doctor::getSpecialization)
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));
        }
        return doctorMap.values().stream()
                .map(Doctor::getSpecialization)
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Map<Integer, Doctor> getDoctorMap() {
        if (doctorDao != null) {
            return doctorDao.findAll().stream()
                    .collect(Collectors.toUnmodifiableMap(Doctor::getId, d -> d));
        }
        return Collections.unmodifiableMap(doctorMap);
    }
}
