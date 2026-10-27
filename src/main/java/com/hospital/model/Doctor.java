package com.hospital.model;

/**
 * ============================================================================
 * JAVA OOP CONCEPTS DEMONSTRATED HERE:
 * ============================================================================
 * 1. INHERITANCE:
 *    - `Doctor extends Person`: Reuses all identity and contact management from Person.
 *    - A Doctor "IS-A" Person.
 *
 * 2. ENCAPSULATION WITH BUSINESS VALIDATION:
 *    - Fields like `consultationFee` have validation checks inside setters
 *      to ensure negative fees cannot be assigned.
 *
 * 3. METHOD OVERRIDING (@Override):
 *    - Overrides `getRole()` to return "Doctor".
 *    - Overrides `getDisplayInfo()` to provide physician credentials and fee info.
 * ============================================================================
 */
public class Doctor extends Person {

    // ------------------------------------------------------------------------
    // DOCTOR-SPECIFIC ENCAPSULATED FIELDS
    // ------------------------------------------------------------------------
    private String specialization;
    private String licenseNumber;
    private double consultationFee;
    private String department;

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    /**
     * Default No-Arg Constructor.
     */
    public Doctor() {
        super();
        this.specialization = "General Medicine";
        this.licenseNumber = "PENDING";
        this.consultationFee = 50.0;
        this.department = "General OPD";
    }

    /**
     * Minimal Convenience Constructor (Constructor Chaining).
     *
     * @param id             Doctor ID
     * @param firstName      First name
     * @param lastName       Last name
     * @param specialization Medical specialization
     */
    public Doctor(int id, String firstName, String lastName, String specialization) {
        this(id, firstName, lastName, "", "", "", specialization, "N/A", 50.0, "General OPD");
    }

    /**
     * Full Parameterized Constructor.
     * Calls parent `super(...)` and initializes doctor-specific attributes.
     */
    public Doctor(int id, String firstName, String lastName, String email, String phoneNumber,
                  String address, String specialization, String licenseNumber,
                  double consultationFee, String department) {
        super(id, firstName, lastName, email, phoneNumber, address);
        this.specialization = (specialization != null) ? specialization.trim() : "General Medicine";
        this.licenseNumber = (licenseNumber != null) ? licenseNumber.trim() : "PENDING";
        setConsultationFee(consultationFee); // Enforces validation
        this.department = (department != null) ? department.trim() : "General";
    }

    // ------------------------------------------------------------------------
    // METHOD OVERRIDING (@Override)
    // ------------------------------------------------------------------------

    /**
     * Implementation of abstract role method from Person.
     */
    @Override
    public String getRole() {
        return "Doctor";
    }

    /**
     * Overrides Displayable contract to format physician details.
     */
    @Override
    public String getDisplayInfo() {
        return String.format("[Dr. %s] Dept: %s | Spec: %s | Fee: $%.2f | Contact: %s",
                getFullName(), department, specialization, consultationFee, getPhoneNumber());
    }

    // ------------------------------------------------------------------------
    // GETTERS AND SETTERS (ENCAPSULATION WITH VALIDATION)
    // ------------------------------------------------------------------------

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = (specialization != null) ? specialization.trim() : "General Medicine";
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = (licenseNumber != null) ? licenseNumber.trim() : "";
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    /**
     * Validates that consultation fee cannot be negative.
     *
     * @param consultationFee the fee to set
     */
    public void setConsultationFee(double consultationFee) {
        if (consultationFee < 0.0) {
            this.consultationFee = 0.0;
        } else {
            this.consultationFee = consultationFee;
        }
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = (department != null) ? department.trim() : "General";
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + getId() +
                ", fullName='Dr. " + getFullName() + '\'' +
                ", specialization='" + specialization + '\'' +
                ", department='" + department + '\'' +
                ", consultationFee=" + consultationFee +
                '}';
    }
}
