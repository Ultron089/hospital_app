package com.hospital.dao.impl;

import com.hospital.dao.DoctorDao;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Doctor;
import com.hospital.util.DbConnectionManager;

import java.sql.*;
import java.util.*;

/**
 * Concrete JDBC implementation of DoctorDao using PreparedStatement.
 */
public class DoctorDaoJdbcImpl implements DoctorDao {

    @Override
    public Doctor save(Doctor doctor) {
        String insertSpecSql = "INSERT INTO specializations (name, description) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = name";
        String insertDocSql = """
            INSERT INTO doctors (first_name, last_name, email, phone_number, address,
                                 specialization, license_number, consultation_fee, department)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DbConnectionManager.getConnection()) {
            // 1. Ensure specialization exists in specializations lookup table to satisfy FK
            try (PreparedStatement psSpec = conn.prepareStatement(insertSpecSql)) {
                psSpec.setString(1, doctor.getSpecialization());
                psSpec.setString(2, doctor.getSpecialization() + " Department");
                psSpec.executeUpdate();
            }

            // 2. Insert Doctor record
            try (PreparedStatement psDoc = conn.prepareStatement(insertDocSql, Statement.RETURN_GENERATED_KEYS)) {
                psDoc.setString(1, doctor.getFirstName());
                psDoc.setString(2, doctor.getLastName());
                psDoc.setString(3, doctor.getEmail());
                psDoc.setString(4, doctor.getPhoneNumber());
                psDoc.setString(5, doctor.getAddress());
                psDoc.setString(6, doctor.getSpecialization());
                psDoc.setString(7, doctor.getLicenseNumber());
                psDoc.setDouble(8, doctor.getConsultationFee());
                psDoc.setString(9, doctor.getDepartment());

                psDoc.executeUpdate();

                try (ResultSet rs = psDoc.getGeneratedKeys()) {
                    if (rs.next()) {
                        doctor.setId(rs.getInt(1));
                    }
                }
            }
            return doctor;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || "23000".equals(e.getSQLState()) || (e.getMessage() != null && e.getMessage().contains("Unique"))) {
                throw new DataAccessException("A doctor with medical license number '" + doctor.getLicenseNumber() + "' is already registered.", e);
            }
            throw new DataAccessException("Failed to insert doctor record: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error saving doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Doctor> findById(Integer id) {
        String sql = "SELECT * FROM doctors WHERE id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDoctor(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to query doctor by ID: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error querying doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Doctor> findAll() {
        String sql = "SELECT * FROM doctors ORDER BY id ASC";
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToDoctor(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to query all doctors: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error querying doctors: " + e.getMessage(), e);
        }
    }

    @Override
    public Doctor update(Doctor doctor) {
        String insertSpecSql = "INSERT INTO specializations (name, description) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = name";
        String updateDocSql = """
            UPDATE doctors
            SET first_name = ?, last_name = ?, email = ?, phone_number = ?, address = ?,
                specialization = ?, license_number = ?, consultation_fee = ?, department = ?
            WHERE id = ?
        """;

        try (Connection conn = DbConnectionManager.getConnection()) {
            // Ensure specialization exists
            try (PreparedStatement psSpec = conn.prepareStatement(insertSpecSql)) {
                psSpec.setString(1, doctor.getSpecialization());
                psSpec.setString(2, doctor.getSpecialization() + " Department");
                psSpec.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(updateDocSql)) {
                ps.setString(1, doctor.getFirstName());
                ps.setString(2, doctor.getLastName());
                ps.setString(3, doctor.getEmail());
                ps.setString(4, doctor.getPhoneNumber());
                ps.setString(5, doctor.getAddress());
                ps.setString(6, doctor.getSpecialization());
                ps.setString(7, doctor.getLicenseNumber());
                ps.setDouble(8, doctor.getConsultationFee());
                ps.setString(9, doctor.getDepartment());
                ps.setInt(10, doctor.getId());

                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new DataAccessException("No doctor found with ID: " + doctor.getId());
                }
            }
            return doctor;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || "23000".equals(e.getSQLState())) {
                throw new DataAccessException("A doctor with medical license number '" + doctor.getLicenseNumber() + "' is already registered.", e);
            }
            throw new DataAccessException("Failed to update doctor: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error updating doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM doctors WHERE id = ?";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete doctor: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error deleting doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Doctor> findByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            return findAll();
        }
        String sql = "SELECT * FROM doctors WHERE LOWER(department) = LOWER(?) ORDER BY id ASC";
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, department.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDoctor(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to query doctors by department: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error querying doctors by department: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Doctor> findBySpecialization(String specialization) {
        if (specialization == null || specialization.trim().isEmpty()) {
            return findAll();
        }
        String sql = "SELECT * FROM doctors WHERE LOWER(specialization) = LOWER(?) ORDER BY id ASC";
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, specialization.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDoctor(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to query doctors by specialization: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error querying doctors by specialization: " + e.getMessage(), e);
        }
    }

    public Doctor findByLicenseNumber(String licenseNumber) {
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT * FROM doctors WHERE LOWER(license_number) = LOWER(?)";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, licenseNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDoctor(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to find doctor by license: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error finding doctor: " + e.getMessage(), e);
        }
    }

    public List<Doctor> searchDoctors(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        String sql = """
            SELECT * FROM doctors
            WHERE LOWER(first_name) LIKE ?
               OR LOWER(last_name) LIKE ?
               OR LOWER(CONCAT(first_name, ' ', last_name)) LIKE ?
               OR LOWER(specialization) LIKE ?
               OR LOWER(department) LIKE ?
               OR LOWER(license_number) LIKE ?
               OR CAST(id AS CHAR) LIKE ?
            ORDER BY id ASC
        """;
        List<Doctor> list = new ArrayList<>();
        String pattern = "%" + query.trim().toLowerCase() + "%";

        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 7; i++) {
                ps.setString(i, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDoctor(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to search doctors: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error searching doctors: " + e.getMessage(), e);
        }
    }

    public Set<String> getAllSpecializations() {
        Set<String> specs = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        String sql = "SELECT name FROM specializations ORDER BY name ASC";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                specs.add(rs.getString("name"));
            }

            // Also check distinct specializations from doctors table
            String docSpecSql = "SELECT DISTINCT specialization FROM doctors WHERE specialization IS NOT NULL";
            try (PreparedStatement ps2 = conn.prepareStatement(docSpecSql);
                 ResultSet rs2 = ps2.executeQuery()) {
                while (rs2.next()) {
                    specs.add(rs2.getString("specialization"));
                }
            }
            return specs;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to query specializations: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Error querying specializations: " + e.getMessage(), e);
        }
    }

    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getString("address"),
                rs.getString("specialization"),
                rs.getString("license_number"),
                rs.getDouble("consultation_fee"),
                rs.getString("department")
        );
    }
}
