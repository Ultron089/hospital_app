package com.hospital.service.impl;

import com.hospital.dao.AppointmentDao;
import com.hospital.exception.HospitalException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.util.DateTimeUtil;
import com.hospital.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Concrete implementation of AppointmentService managing consultations in memory.
 * Demonstrates:
 * - List for maintaining scheduled appointments
 * - Map for O(1) appointment lookup by ID
 * - Map<Integer, List<Appointment>> for grouping appointments by attending doctor
 * - Slot collision detection preventing duplicate active bookings for the same doctor
 * - Rejection of past appointment dates
 * - Lambda expressions for filtering and grouping
 */
public class AppointmentServiceImpl implements AppointmentService {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentDao appointmentDao;
    private final Map<Integer, Appointment> appointmentMap = new ConcurrentHashMap<>();

    public AppointmentServiceImpl(PatientService patientService, DoctorService doctorService) {
        this(patientService, doctorService, null);
    }

    public AppointmentServiceImpl(PatientService patientService, DoctorService doctorService, AppointmentDao appointmentDao) {
        this.patientService = Objects.requireNonNull(patientService, "PatientService cannot be null");
        this.doctorService = Objects.requireNonNull(doctorService, "DoctorService cannot be null");
        this.appointmentDao = appointmentDao;
        if (appointmentDao == null) {
            initSampleAppointments();
        }
    }
// Constructor overload handled above

    private void initSampleAppointments() {
        try {
            Patient p1 = patientService.getPatientById(101);
            Patient p2 = patientService.getPatientById(102);
            Patient p3 = patientService.getPatientById(103);
            Patient p4 = patientService.getPatientById(104);

            Doctor d1 = doctorService.getDoctorById(201);
            Doctor d2 = doctorService.getDoctorById(202);
            Doctor d3 = doctorService.getDoctorById(203);
            Doctor d4 = doctorService.getDoctorById(204);

            Appointment a1 = new Appointment(1001, p1, d1,
                    LocalDateTime.now().plusDays(1).withHour(9).withMinute(30).withSecond(0).withNano(0),
                    AppointmentStatus.CONFIRMED, "Annual ECG and Cardiac Checkup", "Patient requested morning slot.");
            Appointment a2 = new Appointment(1002, p2, d3,
                    LocalDateTime.now().plusDays(2).withHour(11).withMinute(0).withSecond(0).withNano(0),
                    AppointmentStatus.SCHEDULED, "Persistent Migraine Evaluation", "Review CT scan results.");
            Appointment a3 = new Appointment(1003, p3, d2,
                    LocalDateTime.now().plusDays(3).withHour(14).withMinute(15).withSecond(0).withNano(0),
                    AppointmentStatus.SCHEDULED, "Routine Wellness Exam", "Vaccination update.");
            Appointment a4 = new Appointment(1004, p4, d4,
                    LocalDateTime.now().plusDays(4).withHour(16).withMinute(0).withSecond(0).withNano(0),
                    AppointmentStatus.COMPLETED, "Knee Arthroscopy Follow-up", "Recovery progressing well.");

            appointmentMap.put(a1.getId(), a1);
            appointmentMap.put(a2.getId(), a2);
            appointmentMap.put(a3.getId(), a3);
            appointmentMap.put(a4.getId(), a4);
        } catch (HospitalException ignored) {
            // Sample data fallback
        }
    }

    @Override
    public boolean isDoctorSlotAvailable(int doctorId, LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        if (appointmentDao != null) {
            // Delegate to injected DAO interface for slot availability check
            return appointmentDao.isSlotAvailable(doctorId, dateTime);
        }

        // A slot has a conflict if the same doctor has an active (non-cancelled) appointment at that exact time
        return appointmentMap.values().stream()
                .filter(a -> a.getDoctor().getId() == doctorId)
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .noneMatch(a -> a.getAppointmentDateTime().isEqual(dateTime));
    }

    @Override
    public Appointment bookAppointment(int patientId, int doctorId, LocalDateTime dateTime, String reason)
            throws HospitalException {
        // 1. Verify existence of Patient and Doctor
        Patient patient = patientService.getPatientById(patientId);
        Doctor doctor = doctorService.getDoctorById(doctorId);

        // 2. Validate input constraints
        ValidationUtil.requireNonEmpty(reason, "Reason for appointment");
        ValidationUtil.validateFutureDateTime(dateTime, "Appointment date and time");

        // 3. Collision Prevention: Verify Doctor Slot Availability
        if (!isDoctorSlotAvailable(doctorId, dateTime)) {
            String formattedTime = DateTimeUtil.formatDateTime(dateTime);
            throw new ValidationException("appointmentDateTime",
                    String.format("Dr. %s already has an active appointment scheduled at %s. Please select a different time slot.",
                            doctor.getFullName(), formattedTime));
        }

        // 4. Generate next ID and persist
        if (appointmentDao != null) {
            // DAO will generate ID and handle persistence
            Appointment appointment = new Appointment(0, patient, doctor, dateTime,
                    AppointmentStatus.SCHEDULED, reason.trim(), "");
            return appointmentDao.save(appointment);
        }
        int nextId = appointmentMap.keySet().stream().max(Integer::compareTo).orElse(1000) + 1;
        Appointment appointment = new Appointment(nextId, patient, doctor, dateTime,
                AppointmentStatus.SCHEDULED, reason.trim(), "");
        appointmentMap.put(nextId, appointment);
        return appointment;
    }

    @Override
    public List<Appointment> getAllAppointments() {
        if (appointmentDao != null) {
            return appointmentDao.findAll();
        }
        return new ArrayList<>(appointmentMap.values());
    }

    @Override
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        if (date == null) {
            return getAllAppointments();
        }
        if (appointmentDao != null) {
            return appointmentDao.findByDate(date);
        }
        return appointmentMap.values().stream()
                .filter(a -> a.getAppointmentDateTime().toLocalDate().isEqual(date))
                .collect(Collectors.toList());
    }

    @Override
    public Appointment updateAppointmentStatus(int appointmentId, AppointmentStatus newStatus)
            throws HospitalException {
        if (newStatus == null) {
            throw new ValidationException("status", "Status cannot be null.");
        }
        if (appointmentDao != null) {
            // Update status via DAO
            return appointmentDao.updateStatus(appointmentId, newStatus);
        }
        Appointment apt = appointmentMap.get(appointmentId);
        if (apt == null) {
            throw new ResourceNotFoundException("Appointment", appointmentId);
        }
        apt.setStatus(newStatus);
        return apt;
    }

    @Override
    public Appointment cancelAppointment(int appointmentId) throws HospitalException {
        return updateAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED);
    }

    @Override
    public Map<Integer, Appointment> getAppointmentMap() {
        if (appointmentDao != null) {
            List<Appointment> all = appointmentDao.findAll();
            Map<Integer, Appointment> map = new HashMap<>();
            for (Appointment a : all) {
                map.put(a.getId(), a);
            }
            return Collections.unmodifiableMap(map);
        }
        return Collections.unmodifiableMap(appointmentMap);
    }

    @Override
    public Map<Integer, List<Appointment>> getAppointmentsGroupedByDoctor() {
        // Groups appointments using stream groupingBy collector across all appointments (works in both JDBC and in-memory modes)
        return getAllAppointments().stream()
                .collect(Collectors.groupingBy(a -> a.getDoctor().getId()));
    }
}
