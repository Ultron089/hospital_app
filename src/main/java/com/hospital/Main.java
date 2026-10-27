package com.hospital;

import com.hospital.gui.HospitalApp;

/**
 * ============================================================================
 * APPLICATION ENTRY POINT LAUNCHER
 * ============================================================================
 * In JavaFX (Java 9+), launching directly from a class that extends
 * `javafx.application.Application` without the JavaFX module system on the module-path
 * can throw:
 * "Error: JavaFX runtime components are missing, and are required to run this application".
 *
 * This standard launcher class bypasses that issue by delegating to
 * `HospitalApp.main(args)`.
 * ============================================================================
 */
public class Main {

    public static void main(String[] args) {
        HospitalApp.main(args);
    }
}
