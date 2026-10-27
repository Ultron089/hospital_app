package com.hospital.exception;

/**
 * ============================================================================
 * DATA ACCESS RUNTIME EXCEPTION
 * ============================================================================
 * Unchecked exception thrown by DAO implementations when SQL or connection
 * failures occur, enabling clean DAO interface contracts without checked
 * SQLException propagation.
 * ============================================================================
 */
public class DataAccessException extends RuntimeException {

    private final int errorCode;
    private final String sqlState;

    public DataAccessException(String message) {
        super(message);
        this.errorCode = 0;
        this.sqlState = null;
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
        if (cause instanceof java.sql.SQLException sqlEx) {
            this.errorCode = sqlEx.getErrorCode();
            this.sqlState = sqlEx.getSQLState();
        } else {
            this.errorCode = 0;
            this.sqlState = null;
        }
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getSqlState() {
        return sqlState;
    }
}
