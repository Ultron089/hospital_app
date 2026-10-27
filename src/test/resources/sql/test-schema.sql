-- ============================================================================
-- H2 IN-MEMORY TEST SCHEMA (MODE=MySQL)
-- ============================================================================

CREATE TABLE IF NOT EXISTS specializations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NULL
);

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
);

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
    CONSTRAINT fk_doc_spec FOREIGN KEY (specialization) REFERENCES specializations(name) ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_datetime TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    reason VARCHAR(255) NOT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active_doctor_slot VARCHAR(64) GENERATED ALWAYS AS (
        CASE WHEN status != 'CANCELLED' THEN CONCAT(doctor_id, '_', appointment_datetime) ELSE NULL END
    ),
    CONSTRAINT uq_active_slot UNIQUE (active_doctor_slot),
    CONSTRAINT fk_appt_pat FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_appt_doc FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);
