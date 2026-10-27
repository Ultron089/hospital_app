package com.hospital.util;

import com.hospital.exception.DatabaseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * ============================================================================
 * JDBC DATABASE CONNECTION & CONFIGURATION MANAGER
 * ============================================================================
 * Manages JDBC connections, driver registration, and external configuration.
 *
 * Security & Design Principles:
 * 1. Reads database credentials from local 'db.properties' (external file or classpath).
 * 2. Never hardcodes or logs passwords.
 * 3. Supports programmatic configuration for H2 in-memory automated tests.
 * 4. Provides explicit status diagnostics when MySQL is configured but unreachable.
 * ============================================================================
 */
public final class DbConnectionManager {

    private static final String DEFAULT_CONFIG_FILE = "db.properties";
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    private static String dbDriver = DEFAULT_DRIVER;
    private static boolean configured = false;
    private static boolean driverLoaded = false;
    private static String lastConnectionError = null;

    static {
        loadConfiguration();
    }

    private DbConnectionManager() {
        // Utility class
    }

    /**
     * Attempts to load db.properties from external file first, then classpath.
     */
    public static synchronized void loadConfiguration() {
        Properties props = new Properties();
        boolean loaded = false;

        // 1. Check current working directory for external db.properties
        File localFile = new File(DEFAULT_CONFIG_FILE);
        if (localFile.exists() && localFile.isFile()) {
            try (InputStream in = new FileInputStream(localFile)) {
                props.load(in);
                loaded = true;
            } catch (Exception e) {
                lastConnectionError = "Failed to read external db.properties: " + e.getMessage();
            }
        }

        // 2. If not found locally, check classpath
        if (!loaded) {
            try (InputStream in = DbConnectionManager.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
                if (in != null) {
                    props.load(in);
                    loaded = true;
                }
            } catch (Exception e) {
                lastConnectionError = "Failed to read classpath db.properties: " + e.getMessage();
            }
        }

        if (loaded && props.containsKey("db.url")) {
            dbUrl = props.getProperty("db.url");
            dbUser = props.getProperty("db.user", "");
            dbPassword = props.getProperty("db.password", "");
            dbDriver = props.getProperty("db.driver", DEFAULT_DRIVER);
            configured = true;
            loadDriver();
        } else {
            configured = false;
        }
    }

    /**
     * Programmatic configuration override, primarily for automated tests (e.g. H2).
     */
    public static synchronized void configure(String url, String user, String password, String driver) {
        dbUrl = url;
        dbUser = user;
        dbPassword = password;
        dbDriver = (driver != null && !driver.isEmpty()) ? driver : DEFAULT_DRIVER;
        configured = true;
        lastConnectionError = null;
        loadDriver();
    }

    /**
     * Resets connection configuration back to default file lookup.
     */
    public static synchronized void reset() {
        configured = false;
        driverLoaded = false;
        lastConnectionError = null;
        loadConfiguration();
    }

    private static void loadDriver() {
        try {
            Class.forName(dbDriver);
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            driverLoaded = false;
            lastConnectionError = "JDBC Driver class not found: " + dbDriver;
        }
    }

    /**
     * Checks if database properties are configured.
     *
     * @return true if db.properties was located and parsed
     */
    public static boolean isConfigured() {
        return configured;
    }

    /**
     * Obtains a new database connection.
     *
     * @return active java.sql.Connection
     * @throws DatabaseException if connection fails or database is unreachable
     */
    public static Connection getConnection() throws DatabaseException {
        if (!configured) {
            throw new DatabaseException("Database is not configured. Missing or invalid db.properties.");
        }
        if (!driverLoaded) {
            loadDriver();
            if (!driverLoaded) {
                throw new DatabaseException("Failed to load JDBC driver: " + dbDriver);
            }
        }

        try {
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            lastConnectionError = "Cannot connect to database (" + dbUrl + "): " + e.getMessage();
            throw new DatabaseException("Database connection error: " + e.getMessage(), e, e.getErrorCode(), e.getSQLState());
        }
    }

    /**
     * Tests connectivity to the configured database.
     *
     * @return true if connection succeeded, false otherwise
     */
    public static boolean testConnection() {
        if (!configured) {
            lastConnectionError = "Database is not configured (missing or invalid db.properties).";
            return false;
        }
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                lastConnectionError = null;
                return true;
            }
            lastConnectionError = "Connection established but immediately closed.";
            return false;
        } catch (Exception e) {
            lastConnectionError = (e.getMessage() != null && !e.getMessage().isBlank())
                    ? e.getMessage()
                    : "Failed to connect to database at " + dbUrl;
            return false;
        }
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getLastConnectionError() {
        return lastConnectionError;
    }
}
