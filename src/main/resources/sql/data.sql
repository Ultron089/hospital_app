-- ============================================================================
-- Hospital Appointment Management System - Seed Data (Phase 2)
-- Seeds specializations first, followed by patients, doctors, and appointments.
-- ============================================================================

USE hospital_db;

-- ----------------------------------------------------------------------------
-- 1. SPECIALIZATIONS (Seeded first to satisfy foreign keys)
-- ----------------------------------------------------------------------------
INSERT INTO specializations (name, description) VALUES
('Cardiology', 'Heart and cardiovascular disease management'),
('Pediatrics', 'Comprehensive infant, child, and adolescent healthcare'),
('Neurology', 'Brain, spinal cord, and peripheral nerve disorders'),
('Orthopedics', 'Musculoskeletal system, bone, and joint care'),
('General Medicine', 'Primary care and internal medicine')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ----------------------------------------------------------------------------
-- 2. PATIENTS (Unique phone numbers)
-- ----------------------------------------------------------------------------
INSERT INTO patients (id, first_name, last_name, email, phone_number, address, date_of_birth, blood_group, medical_history, emergency_contact) VALUES
(101, 'Alice', 'Johnson', 'alice.j@example.com', '+1-555-0101', '124 Elm St, Springfield', '1992-04-15', 'A+', 'Asthma, Penicillin Allergy', 'Bob Johnson (+1-555-0102)'),
(102, 'Michael', 'Chen', 'm.chen@example.com', '+1-555-0103', '742 Evergreen Terrace', '1985-11-23', 'O+', 'Hypertension', 'Linda Chen (+1-555-0104)'),
(103, 'Sarah', 'Williams', 'sarah.w@example.com', '+1-555-0105', '58 Oak Ridge Ln', '1998-07-30', 'B-', 'None', 'David Williams (+1-555-0106)'),
(104, 'Robert', 'Garcia', 'r.garcia@example.com', '+1-555-0107', '901 Maple Avenue', '1976-02-09', 'AB+', 'Type-2 Diabetes', 'Maria Garcia (+1-555-0108)')
ON DUPLICATE KEY UPDATE email = VALUES(email), phone_number = VALUES(phone_number);

-- ----------------------------------------------------------------------------
-- 3. DOCTORS (Valid foreign keys to specializations, unique license numbers)
-- ----------------------------------------------------------------------------
INSERT INTO doctors (id, first_name, last_name, email, phone_number, address, specialization, license_number, consultation_fee, department) VALUES
(201, 'Evelyn', 'Reed', 'evelyn.reed@hospital.org', '+1-555-0201', 'Hospital Suites #301', 'Cardiology', 'MD-NY-84920', 150.00, 'Cardiovascular Diseases'),
(202, 'Marcus', 'Vance', 'm.vance@hospital.org', '+1-555-0202', 'Hospital Suites #204', 'Pediatrics', 'MD-NY-77431', 110.00, 'Child & Adolescent Health'),
(203, 'Elena', 'Rostova', 'e.rostova@hospital.org', '+1-555-0203', 'Hospital Suites #405', 'Neurology', 'MD-NY-91823', 175.00, 'Neurosciences'),
(204, 'James', 'Wilson', 'j.wilson@hospital.org', '+1-555-0204', 'Hospital Suites #112', 'Orthopedics', 'MD-NY-63201', 140.00, 'Bone & Joint Surgery')
ON DUPLICATE KEY UPDATE license_number = VALUES(license_number), consultation_fee = VALUES(consultation_fee);

-- ----------------------------------------------------------------------------
-- 4. APPOINTMENTS (Valid foreign keys, unique active slots)
-- ----------------------------------------------------------------------------
INSERT INTO appointments (id, patient_id, doctor_id, appointment_datetime, status, reason, notes) VALUES
(1001, 101, 201, DATE_ADD(NOW(), INTERVAL 1 DAY), 'CONFIRMED', 'Annual ECG and Cardiac Checkup', 'Patient requested morning slot.'),
(1002, 102, 203, DATE_ADD(NOW(), INTERVAL 2 DAY), 'SCHEDULED', 'Persistent Migraine Evaluation', 'Review CT scan results.'),
(1003, 103, 202, DATE_ADD(NOW(), INTERVAL 3 DAY), 'SCHEDULED', 'Routine Wellness Exam', 'Vaccination update.'),
(1004, 104, 204, DATE_ADD(NOW(), INTERVAL 4 DAY), 'COMPLETED', 'Knee Arthroscopy Follow-up', 'Recovery progressing well.')
ON DUPLICATE KEY UPDATE status = VALUES(status), reason = VALUES(reason);
