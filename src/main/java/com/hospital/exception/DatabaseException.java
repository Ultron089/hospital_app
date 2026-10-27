package com.hospital.exception;

/**
 * ============================================================================
 * DATABASE PERSISTENCE EXCEPTION
 * ============================================================================
 * Thrown when database operations, JDBC connections, or SQL transactions fail.
 * Inherits from `HospitalException` to preserve the checked exception hierarchy.
 * ============================================================================
 */
public class DatabaseException extends HospitalException {

    private final int errorCode;
    private final String sqlState;

    public DatabaseException(String message) {
        super(message);
        this.errorCode = 0;
        this.sqlState = null;
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 0;
        this.sqlState = null;
    }

    public DatabaseException(String message, Throwable cause, int errorCode, String sqlState) {
        super(message, cause);
        this.errorCode = errorCode;
        this.sqlState = sqlState;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getSqlState() {
        return sqlState;
    }
}
