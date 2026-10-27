-- ============================================================================
-- Hospital Appointment Management System - Database Schema (Phase 2 MySQL DDL)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS hospital_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE hospital_db;

-- ----------------------------------------------------------------------------
-- 1. SPECIALIZATIONS LOOKUP TABLE
-- Seeded before doctors to satisfy foreign key constraints.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS specializations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 2. PATIENTS TABLE
-- Maps Patient domain model (inherits Person).
-- Unique constraint on phone_number to prevent duplicate registrations.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NULL,
    phone_number VARCHAR(30) NOT NULL UNIQUE,
    address VARCHAR(255) NULL,
    date_of_birth DATE NOT NULL,
    blood_group VARCHAR(10) DEFAULT 'Unknown',
    medical_history TEXT NULL,
    emergency_contact VARCHAR(150) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 3. DOCTORS TABLE
-- Maps Doctor domain model (inherits Person).
-- Unique constraint on license_number.
-- Foreign key to specializations table for referential integrity.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NULL,
    phone_number VARCHAR(30) NULL,
    address VARCHAR(255) NULL,
    specialization VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    consultation_fee DECIMAL(10, 2) NOT NULL DEFAULT 50.00,
    department VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doctor_specialization FOREIGN KEY (specialization)
        REFERENCES specializations(name) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 4. APPOINTMENTS TABLE
-- Demonstrates Composition: patient_id and doctor_id foreign keys.
--
-- Concurrency & Active-Slot Conflict Prevention:
-- Stored generated column `active_doctor_slot` computes `doctor_id_datetime`
-- ONLY when status != 'CANCELLED'.
-- In SQL, NULL values are not considered duplicate in a UNIQUE index.
-- Thus, active appointments for the same doctor at the same slot are strictly
-- rejected at the database level.
-- When an appointment is CANCELLED, `active_doctor_slot` becomes NULL,
-- making the slot available again immediately while keeping full audit history!
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_datetime DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    reason VARCHAR(255) NOT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active_doctor_slot VARCHAR(64) GENERATED ALWAYS AS (
        CASE WHEN status != 'CANCELLED' THEN CONCAT(doctor_id, '_', appointment_datetime) ELSE NULL END
    ) STORED,
    CONSTRAINT uq_active_doctor_slot UNIQUE (active_doctor_slot),
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appt_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Query optimization indexes
CREATE INDEX idx_patient_name ON patients(first_name, last_name);
CREATE INDEX idx_doctor_department ON doctors(department);
CREATE INDEX idx_appt_datetime ON appointments(appointment_datetime);
CREATE INDEX idx_appt_status ON appointments(status);
