package com.hospital.dao.impl;

import com.hospital.dao.PatientDao;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Patient;
import com.hospital.util.DbConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Concrete JDBC implementation of PatientDao using PreparedStatement.
 */
public class PatientDaoJdbcImpl implements PatientDao {

    @Override
    public Patient save(Patient patient) {
        String sql = """
            INSERT INTO patients (first_name, last_name, email, phone_number, address,
                                  date_of_birth, blood_group, medical_history, emergency_contact)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, patient.getFirstName());
            ps.setString(2, patient.getLastName());
            ps.setString(3, patient.getEmail());
            ps.setString(4, patient.getPhoneNumber());
            ps.setString(5, patient.getAddress());
            ps.setDate(6, patient.getDateOfBirth() != null ? Date.valueOf(patient.getDateOfBirth()) : Date.valueOf(LocalDate.now()));
            ps.setString(7, patient.getBloodGroup());
            ps.setString(8, patient.getMedicalHistory());
            ps.setString(9, patient.getEmergencyContact());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    patient.setId(rs.getInt(1));
                }
            }
            return patient;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || "23000".equals(e.getSQLState()) || (e.getMessage() != null && e.getMessage().contains("Unique"))) {
                throw new DataAccessException("A patient with phone number '" + patient.getPhoneNumber() + "' is already registered.", e);
            }
            throw new DataAccessException("Failed to insert patient record: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error saving patient: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Patient> findById(Integer id) {
        String sql = "SELECT * FROM patients WHERE id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to query patient by ID: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error accessing database: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Patient> findAll() {
        String sql = "SELECT * FROM patients ORDER BY id ASC";
        List<Patient> list = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToPatient(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to query all patients: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error querying patients: " + e.getMessage(), e);
        }
    }

    @Override
    public Patient update(Patient patient) {
        String sql = """
            UPDATE patients
            SET first_name = ?, last_name = ?, email = ?, phone_number = ?, address = ?,
                date_of_birth = ?, blood_group = ?, medical_history = ?, emergency_contact = ?
            WHERE id = ?
        """;

        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patient.getFirstName());
            ps.setString(2, patient.getLastName());
            ps.setString(3, patient.getEmail());
            ps.setString(4, patient.getPhoneNumber());
            ps.setString(5, patient.getAddress());
            ps.setDate(6, patient.getDateOfBirth() != null ? Date.valueOf(patient.getDateOfBirth()) : Date.valueOf(LocalDate.now()));
            ps.setString(7, patient.getBloodGroup());
            ps.setString(8, patient.getMedicalHistory());
            ps.setString(9, patient.getEmergencyContact());
            ps.setInt(10, patient.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("No patient found with ID: " + patient.getId());
            }
            return patient;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || "23000".equals(e.getSQLState())) {
                throw new DataAccessException("A patient with phone number '" + patient.getPhoneNumber() + "' is already registered.", e);
            }
            throw new DataAccessException("Failed to update patient: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error updating patient: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM patients WHERE id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete patient: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error deleting patient: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Patient> searchByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        String sql = """
            SELECT * FROM patients
            WHERE LOWER(first_name) LIKE ?
               OR LOWER(last_name) LIKE ?
               OR LOWER(CONCAT(first_name, ' ', last_name)) LIKE ?
               OR phone_number LIKE ?
               OR LOWER(email) LIKE ?
               OR CAST(id AS CHAR) LIKE ?
            ORDER BY id ASC
        """;
        List<Patient> list = new ArrayList<>();
        String pattern = "%" + query.trim().toLowerCase() + "%";

        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            ps.setString(6, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPatient(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to search patients: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error searching patients: " + e.getMessage(), e);
        }
    }

    @Override
    public Patient findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT * FROM patients WHERE phone_number = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phoneNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to find patient by phone number: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error finding patient: " + e.getMessage(), e);
        }
    }

    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Date dob = rs.getDate("date_of_birth");
        LocalDate birthDate = (dob != null) ? dob.toLocalDate() : LocalDate.now();

        return new Patient(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getString("address"),
                birthDate,
                rs.getString("blood_group"),
                rs.getString("medical_history"),
                rs.getString("emergency_contact")
        );
    }
}
