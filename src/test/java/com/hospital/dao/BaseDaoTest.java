package com.hospital.dao;

import com.hospital.util.DbConnectionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Base test class setting up an in-memory H2 database in MySQL compatibility mode.
 * Ensures unit and DAO tests run reliably and safely without requiring live MySQL permissions.
 */
public abstract class BaseDaoTest {

    private static final String H2_URL = "jdbc:h2:mem:hospital_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    private static final String H2_DRIVER = "org.h2.Driver";

    @BeforeAll
    static void initDatabase() throws Exception {
        DbConnectionManager.configure(H2_URL, "sa", "", H2_DRIVER);

        // Execute DDL schema
        try (InputStream in = BaseDaoTest.class.getResourceAsStream("/sql/test-schema.sql");
             Connection conn = DbConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {

            if (in == null) {
                throw new IllegalStateException("Cannot find /sql/test-schema.sql on test classpath");
            }

            String ddl = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Split into statements
            for (String statement : ddl.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    @BeforeEach
    void resetTestData() throws Exception {
        try (Connection conn = DbConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Clean tables in reverse dependency order
            stmt.execute("DELETE FROM appointments");
            stmt.execute("DELETE FROM doctors");
            stmt.execute("DELETE FROM patients");
            stmt.execute("DELETE FROM specializations");

            // Re-seed standard specializations lookup
            stmt.execute("INSERT INTO specializations (name, description) VALUES ('Cardiology', 'Heart Care')");
            stmt.execute("INSERT INTO specializations (name, description) VALUES ('Pediatrics', 'Child Health')");
            stmt.execute("INSERT INTO specializations (name, description) VALUES ('Neurology', 'Brain Care')");
            stmt.execute("INSERT INTO specializations (name, description) VALUES ('Orthopedics', 'Bone Care')");
            stmt.execute("INSERT INTO specializations (name, description) VALUES ('General Medicine', 'General OPD')");
        }
    }
}
