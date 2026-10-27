package com.hospital.service.impl;

import com.hospital.dao.PatientDao;
import com.hospital.exception.DataAccessException;
import com.hospital.exception.HospitalException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Patient;
import com.hospital.service.PatientService;
import com.hospital.util.ValidationUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Concrete implementation of PatientService.
 * Supports both Database persistence (via PatientDao) and In-Memory fallback.
 */
public class PatientServiceImpl implements PatientService {

    private final PatientDao patientDao;
    private final Map<Integer, Patient> patientMap = new ConcurrentHashMap<>();

    public PatientServiceImpl() {
        this(null);
    }

    public PatientServiceImpl(PatientDao patientDao) {
        this.patientDao = patientDao;
        if (patientDao == null) {
            initSamplePatients();
        }
    }

    private void initSamplePatients() {
        addSample(new Patient(101, "Alice", "Johnson", "alice.j@example.com", "+1-555-0101",
                "124 Elm St, Springfield", LocalDate.of(1992, 4, 15), "A+", "Asthma, Penicillin Allergy", "Bob Johnson (+1-555-0102)"));
        addSample(new Patient(102, "Michael", "Chen", "m.chen@example.com", "+1-555-0103",
                "742 Evergreen Terrace", LocalDate.of(1985, 11, 23), "O+", "Hypertension", "Linda Chen (+1-555-0104)"));
        addSample(new Patient(103, "Sarah", "Williams", "sarah.w@example.com", "+1-555-0105",
                "58 Oak Ridge Ln", LocalDate.of(1998, 7, 30), "B-", "None", "David Williams (+1-555-0106)"));
        addSample(new Patient(104, "Robert", "Garcia", "r.garcia@example.com", "+1-555-0107",
                "901 Maple Avenue", LocalDate.of(1976, 2, 9), "AB+", "Type-2 Diabetes", "Maria Garcia (+1-555-0108)"));
    }

    private void addSample(Patient p) {
        patientMap.put(p.getId(), p);
    }

    @Override
    public Patient registerPatient(Patient patient) throws HospitalException {
        if (patient == null) {
            throw new HospitalException("Patient object cannot be null.");
        }

        // Validate required business constraints
        ValidationUtil.requireNonEmpty(patient.getFirstName(), "First Name");
        ValidationUtil.requireNonEmpty(patient.getLastName(), "Last Name");
        ValidationUtil.requireNonEmpty(patient.getPhoneNumber(), "Phone Number");
        ValidationUtil.validatePhoneNumber(patient.getPhoneNumber());
        ValidationUtil.validateEmail(patient.getEmail());
        ValidationUtil.validateDateOfBirth(patient.getDateOfBirth());

        String phone = patient.getPhoneNumber().trim();

        if (patientDao != null) {
            Patient existing = patientDao.findByPhoneNumber(phone);
            if (existing != null) {
                throw new ValidationException("phoneNumber",
                        "A patient with phone number '" + phone + "' is already registered.");
            }

            try {
                return patientDao.save(patient);
            } catch (DataAccessException e) {
                if (e.getMessage() != null && e.getMessage().contains("already registered")) {
                    throw new ValidationException("phoneNumber", e.getMessage());
                }
                throw new HospitalException(e.getMessage(), e);
            }
        }

        // In-Memory Fallback
        boolean phoneExists = patientMap.values().stream()
                .anyMatch(p -> p.getPhoneNumber() != null && p.getPhoneNumber().equalsIgnoreCase(phone));
        if (phoneExists) {
            throw new ValidationException("phoneNumber",
                    "A patient with phone number '" + phone + "' is already registered.");
        }

        int nextId = patientMap.keySet().stream().max(Integer::compareTo).orElse(100) + 1;
        patient.setId(nextId);
        patientMap.put(nextId, patient);
        return patient;
    }

    @Override
    public List<Patient> getAllPatients() {
        if (patientDao != null) {
            return patientDao.findAll();
        }
        return new ArrayList<>(patientMap.values());
    }

    @Override
    public Patient getPatientById(int id) throws HospitalException {
        if (patientDao != null) {
            return patientDao.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        }
        Patient patient = patientMap.get(id);
        if (patient == null) {
            throw new ResourceNotFoundException("Patient", id);
        }
        return patient;
    }

    @Override
    public List<Patient> searchPatients(String query) {
        if (patientDao != null) {
            return patientDao.searchByName(query);
        }
        if (query == null || query.trim().isEmpty()) {
            return getAllPatients();
        }
        String q = query.trim().toLowerCase();
        return patientMap.values().stream()
                .filter(p -> p.getFullName().toLowerCase().contains(q)
                        || p.getPhoneNumber().toLowerCase().contains(q)
                        || p.getEmail().toLowerCase().contains(q)
                        || String.valueOf(p.getId()).contains(q)
                        || p.getBloodGroup().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    @Override
    public Map<Integer, Patient> getPatientMap() {
        if (patientDao != null) {
            return patientDao.findAll().stream()
                    .collect(Collectors.toUnmodifiableMap(Patient::getId, p -> p));
        }
        return Collections.unmodifiableMap(patientMap);
    }
}
