package com.hospital.service;

import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.util.DbConnectionManager;
import com.hospital.model.Patient;

import java.util.List;

/**
 * In-memory sample data provider for Phase 1 demonstration and GUI preview.
 * Allows the JavaFX GUI navigation tabs to display live sample data cards and tables
 * without requiring a live MySQL database in Phase 1.
 */
public class MockDataService {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    public MockDataService() {
        if (DbConnectionManager.isConfigured()) {
            // Verify database connection before proceeding
            boolean connected = false;
            try {
                connected = DbConnectionManager.testConnection();
            } catch (Exception e) {
                String err = (e.getMessage() != null && !e.getMessage().isBlank()) ? e.getMessage() : "Unexpected error during connection test";
                throw new IllegalStateException("Database configuration detected in db.properties, but connection failed: " + err, e);
            }

            if (!connected) {
                String err = DbConnectionManager.getLastConnectionError();
                if (err == null || err.isBlank()) {
                    err = "Failed to establish connection to configured database (" + DbConnectionManager.getDbUrl() + ").";
                }
                throw new IllegalStateException("Database configuration detected in db.properties, but connection failed: " + err);
            }

            // JDBC DAOs
            com.hospital.dao.PatientDao patientDao = new com.hospital.dao.impl.PatientDaoJdbcImpl();
            com.hospital.dao.DoctorDao doctorDao = new com.hospital.dao.impl.DoctorDaoJdbcImpl();
            com.hospital.dao.AppointmentDao appointmentDao = new com.hospital.dao.impl.AppointmentDaoJdbcImpl();
            this.patientService = new com.hospital.service.impl.PatientServiceImpl(patientDao);
            this.doctorService = new com.hospital.service.impl.DoctorServiceImpl(doctorDao);
            this.appointmentService = new com.hospital.service.impl.AppointmentServiceImpl(patientService, doctorService, appointmentDao);
        } else {
            this.patientService = new com.hospital.service.impl.PatientServiceImpl();
            this.doctorService = new com.hospital.service.impl.DoctorServiceImpl();
            this.appointmentService = new com.hospital.service.impl.AppointmentServiceImpl(patientService, doctorService);
        }
    }

    public MockDataService(PatientService patientService, DoctorService doctorService, AppointmentService appointmentService) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }

    public PatientService getPatientService() {
        return patientService;
    }

    public DoctorService getDoctorService() {
        return doctorService;
    }

    public AppointmentService getAppointmentService() {
        return appointmentService;
    }

    public List<Patient> getPatients() {
        return patientService.getAllPatients();
    }

    public List<Doctor> getDoctors() {
        return doctorService.getAllDoctors();
    }

    public List<Appointment> getAppointments() {
        return appointmentService.getAllAppointments();
    }
}
