package com.hospital.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * ============================================================================
 * JAVA OOP CONCEPTS DEMONSTRATED HERE:
 * ============================================================================
 * 1. INHERITANCE:
 *    - Uses the `extends` keyword: `Patient extends Person`.
 *    - A Patient "IS-A" Person.
 *    - Patient inherits all non-private behaviors and properties of Person
 *      (e.g., id, firstName, lastName, getFullName(), getEmail()).
 *
 * 2. CONSTRUCTOR CHAINING (super & this):
 *    - `super(...)` calls the constructor of the parent class (Person).
 *    - In Java, `super(...)` MUST be the very first statement in the constructor.
 *    - `this(...)` can be used to call another constructor within the same class.
 *
 * 3. METHOD OVERRIDING (@Override):
 *    - Implements the abstract method `getRole()` inherited from `Person`.
 *    - Overrides `getDisplayInfo()` and `toString()` with patient-specific logic.
 * ============================================================================
 */
public class Patient extends Person {

    // ------------------------------------------------------------------------
    // PATIENT-SPECIFIC ENCAPSULATED FIELDS
    // ------------------------------------------------------------------------
    private LocalDate dateOfBirth;
    private String bloodGroup;
    private String medicalHistory;
    private String emergencyContact;

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    /**
     * Default No-Arg Constructor.
     * Calls super() implicitly and initializes default patient values.
     */
    public Patient() {
        super();
        this.dateOfBirth = LocalDate.now();
        this.bloodGroup = "Unknown";
        this.medicalHistory = "None recorded";
        this.emergencyContact = "";
    }

    /**
     * Minimal Convenience Constructor (Constructor Chaining using `this(...)`).
     *
     * @param id          Patient ID
     * @param firstName   First name
     * @param lastName    Last name
     * @param phoneNumber Contact number
     */
    public Patient(int id, String firstName, String lastName, String phoneNumber) {
        this(id, firstName, lastName, "", phoneNumber, "", LocalDate.of(2000, 1, 1), "Unknown", "None", "");
    }

    /**
     * Full Parameterized Constructor.
     * Uses `super(...)` to initialize parent Person attributes, then initializes
     * patient-specific attributes.
     */
    public Patient(int id, String firstName, String lastName, String email, String phoneNumber,
                   String address, LocalDate dateOfBirth, String bloodGroup,
                   String medicalHistory, String emergencyContact) {
        // Calls the parent Person constructor
        super(id, firstName, lastName, email, phoneNumber, address);
        this.dateOfBirth = (dateOfBirth != null) ? dateOfBirth : LocalDate.now();
        this.bloodGroup = (bloodGroup != null) ? bloodGroup : "Unknown";
        this.medicalHistory = (medicalHistory != null) ? medicalHistory : "None";
        this.emergencyContact = (emergencyContact != null) ? emergencyContact : "";
    }

    // ------------------------------------------------------------------------
    // METHOD OVERRIDING (@Override)
    // ------------------------------------------------------------------------

    /**
     * Implementation of abstract method from Person.
     */
    @Override
    public String getRole() {
        return "Patient";
    }

    /**
     * Overrides Displayable contract to show patient details in UI.
     */
    @Override
    public String getDisplayInfo() {
        return String.format("[Patient #%d] %s (Age: %d, Blood: %s) | Contact: %s",
                getId(), getFullName(), getAge(), bloodGroup, getPhoneNumber());
    }

    /**
     * Calculates patient's age in years based on dateOfBirth.
     *
     * @return Age in years
     */
    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    // ------------------------------------------------------------------------
    // GETTERS AND SETTERS (ENCAPSULATION)
    // ------------------------------------------------------------------------

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = (dateOfBirth != null) ? dateOfBirth : LocalDate.now();
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = (bloodGroup != null) ? bloodGroup.trim() : "Unknown";
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = (medicalHistory != null) ? medicalHistory.trim() : "None";
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = (emergencyContact != null) ? emergencyContact.trim() : "";
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + getId() +
                ", fullName='" + getFullName() + '\'' +
                ", age=" + getAge() +
                ", bloodGroup='" + bloodGroup + '\'' +
                ", emergencyContact='" + emergencyContact + '\'' +
                '}';
    }
}
