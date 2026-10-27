package com.hospital.dao.impl;

import com.hospital.dao.AppointmentDao;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.util.DbConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Concrete JDBC implementation of AppointmentDao using PreparedStatement and SQL JOINs.
 * Enforces database-level active-slot conflict prevention and cancellation history preservation.
 */
public class AppointmentDaoJdbcImpl implements AppointmentDao {

    private static final String BASE_SELECT_SQL = """
        SELECT a.id AS appt_id, a.appointment_datetime, a.status AS appt_status,
               a.reason AS appt_reason, a.notes AS appt_notes,
               p.id AS p_id, p.first_name AS p_first_name, p.last_name AS p_last_name, p.email AS p_email,
               p.phone_number AS p_phone, p.address AS p_address, p.date_of_birth AS p_dob,
               p.blood_group AS p_blood, p.medical_history AS p_med_hist, p.emergency_contact AS p_emergency,
               d.id AS d_id, d.first_name AS d_first_name, d.last_name AS d_last_name, d.email AS d_email,
               d.phone_number AS d_phone, d.address AS d_address, d.specialization AS d_specialization,
               d.license_number AS d_license, d.consultation_fee AS d_fee, d.department AS d_department
        FROM appointments a
        JOIN patients p ON a.patient_id = p.id
        JOIN doctors d ON a.doctor_id = d.id
    """;

    @Override
    public Appointment save(Appointment appointment) {
        String insertSql = """
            INSERT INTO appointments (patient_id, doctor_id, appointment_datetime, status, reason, notes)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, appointment.getPatient().getId());
            ps.setInt(2, appointment.getDoctor().getId());
            ps.setTimestamp(3, Timestamp.valueOf(appointment.getAppointmentDateTime()));
            ps.setString(4, appointment.getStatus().name());
            ps.setString(5, appointment.getReason());
            ps.setString(6, appointment.getNotes());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    appointment.setId(rs.getInt(1));
                }
            }
            return appointment;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || "23000".equals(e.getSQLState()) || (e.getMessage() != null && e.getMessage().contains("active_doctor_slot"))) {
                String doctorName = appointment.getDoctor() != null ? appointment.getDoctor().getFullName() : "Doctor";
                throw new DataAccessException("Dr. " + doctorName + " already has an active appointment scheduled at " + appointment.getFormattedDateTime() + ". Please select a different time slot.", e);
            }
            throw new DataAccessException("Failed to book appointment: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error booking appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Appointment> findById(Integer id) {
        String sql = BASE_SELECT_SQL + " WHERE a.id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAppointment(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to query appointment by ID: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error accessing appointments: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Appointment> findAll() {
        String sql = BASE_SELECT_SQL + " ORDER BY a.appointment_datetime ASC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToAppointment(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to query all appointments: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error querying appointments: " + e.getMessage(), e);
        }
    }

    @Override
    public Appointment update(Appointment appointment) {
        String sql = """
            UPDATE appointments
            SET patient_id = ?, doctor_id = ?, appointment_datetime = ?,
                status = ?, reason = ?, notes = ?
            WHERE id = ?
        """;

        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointment.getPatient().getId());
            ps.setInt(2, appointment.getDoctor().getId());
            ps.setTimestamp(3, Timestamp.valueOf(appointment.getAppointmentDateTime()));
            ps.setString(4, appointment.getStatus().name());
            ps.setString(5, appointment.getReason());
            ps.setString(6, appointment.getNotes());
            ps.setInt(7, appointment.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("No appointment found with ID: " + appointment.getId());
            }
            return appointment;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || "23000".equals(e.getSQLState()) || (e.getMessage() != null && e.getMessage().contains("active_doctor_slot"))) {
                String doctorName = appointment.getDoctor() != null ? appointment.getDoctor().getFullName() : "Doctor";
                throw new DataAccessException("Dr. " + doctorName + " already has an active appointment scheduled at " + appointment.getFormattedDateTime() + ". Please select a different time slot.", e);
            }
            throw new DataAccessException("Failed to update appointment: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error updating appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM appointments WHERE id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete appointment: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error deleting appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) {
        if (date == null) {
            return findAll();
        }
        String sql = BASE_SELECT_SQL + " WHERE DATE(a.appointment_datetime) = ? ORDER BY a.appointment_datetime ASC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to query appointments by date: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error querying appointments: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Appointment> findByPatientId(int patientId) {
        String sql = BASE_SELECT_SQL + " WHERE a.patient_id = ? ORDER BY a.appointment_datetime ASC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to query appointments by patient ID: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error querying appointments: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Appointment> findByDoctorId(int doctorId) {
        String sql = BASE_SELECT_SQL + " WHERE a.doctor_id = ? ORDER BY a.appointment_datetime ASC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to query appointments by doctor ID: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error querying appointments: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Appointment> findByStatus(AppointmentStatus status) {
        if (status == null) {
            return findAll();
        }
        String sql = BASE_SELECT_SQL + " WHERE a.status = ? ORDER BY a.appointment_datetime ASC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to query appointments by status: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error querying appointments: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a doctor has an active (non-cancelled) appointment at the specified date and time.
     */
    @Override
    public boolean isSlotAvailable(int doctorId, LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_datetime = ? AND status != 'CANCELLED'";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setTimestamp(2, Timestamp.valueOf(dateTime));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
            return true;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to check doctor slot availability: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error checking slot availability: " + e.getMessage(), e);
        }
    }

    /**
     * Updates only the appointment status (e.g. CANCELLED, COMPLETED).
     */
    public Appointment updateStatus(int appointmentId, AppointmentStatus newStatus) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus.name());
            ps.setInt(2, appointmentId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("No appointment found with ID: " + appointmentId);
            }
            return findById(appointmentId).orElseThrow(() ->
                    new DataAccessException("Updated appointment could not be retrieved: #" + appointmentId));

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update appointment status: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error updating appointment status: " + e.getMessage(), e);
        }
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Date pDob = rs.getDate("p_dob");
        LocalDate patientDob = (pDob != null) ? pDob.toLocalDate() : LocalDate.now();

        Patient patient = new Patient(
                rs.getInt("p_id"),
                rs.getString("p_first_name"),
                rs.getString("p_last_name"),
                rs.getString("p_email"),
                rs.getString("p_phone"),
                rs.getString("p_address"),
                patientDob,
                rs.getString("p_blood"),
                rs.getString("p_med_hist"),
                rs.getString("p_emergency")
        );

        Doctor doctor = new Doctor(
                rs.getInt("d_id"),
                rs.getString("d_first_name"),
                rs.getString("d_last_name"),
                rs.getString("d_email"),
                rs.getString("d_phone"),
                rs.getString("d_address"),
                rs.getString("d_specialization"),
                rs.getString("d_license"),
                rs.getDouble("d_fee"),
                rs.getString("d_department")
        );

        Timestamp ts = rs.getTimestamp("appointment_datetime");
        LocalDateTime dateTime = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

        String statusStr = rs.getString("appt_status");
        AppointmentStatus status = AppointmentStatus.SCHEDULED;
        if (statusStr != null) {
            try {
                status = AppointmentStatus.valueOf(statusStr);
            } catch (IllegalArgumentException ignored) {}
        }

        return new Appointment(
                rs.getInt("appt_id"),
                patient,
                doctor,
                dateTime,
                status,
                rs.getString("appt_reason"),
                rs.getString("appt_notes")
        );
    }
}
