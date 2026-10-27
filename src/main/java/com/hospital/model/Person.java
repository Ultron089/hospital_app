package com.hospital.model;

import java.util.Objects;

/**
 * ============================================================================
 * JAVA OOP CONCEPTS DEMONSTRATED HERE:
 * ============================================================================
 * 1. ABSTRACTION:
 *    - Marked with the `abstract` keyword.
 *    - An abstract class represents a general concept ("Person").
 *    - It CANNOT be instantiated directly using `new Person()`.
 *    - It serves as a blueprint for concrete subclasses like `Patient` and `Doctor`.
 *    - Contains the abstract method `getRole()`, forcing each subclass to define its role.
 *
 * 2. ENCAPSULATION:
 *    - All instance variables (fields) are declared `private`.
 *    - Direct external access to data is blocked to prevent accidental corruption.
 *    - Access is strictly controlled through public `getter` and `setter` methods,
 *      which can include validation rules.
 *
 * 3. INTERFACES:
 *    - Implements `Identifiable` (guaranteeing `getId()` and `setId()`)
 *    - Implements `Displayable` (guaranteeing `getDisplayInfo()`)
 * ============================================================================
 */
public abstract class Person implements Identifiable, Displayable {

    // ------------------------------------------------------------------------
    // ENCAPSULATION: Private instance variables (data hiding)
    // ------------------------------------------------------------------------
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    /**
     * Default No-Argument Constructor.
     * Useful for frameworks, serialization, or when creating an empty entity.
     */
    public Person() {
        this.id = 0;
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.phoneNumber = "";
        this.address = "";
    }

    /**
     * Parameterized Constructor.
     * Initializes all common attributes for any Person in the system.
     *
     * @param id          Unique person ID
     * @param firstName   First name
     * @param lastName    Last name
     * @param email       Email address
     * @param phoneNumber Contact telephone number
     * @param address     Physical residential address
     */
    public Person(int id, String firstName, String lastName, String email, String phoneNumber, String address) {
        this.id = id;
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setAddress(address);
    }

    // ------------------------------------------------------------------------
    // ABSTRACT METHOD (ABSTRACTION)
    // ------------------------------------------------------------------------

    /**
     * Abstract method that MUST be implemented by every concrete subclass.
     * A Patient will return "Patient", a Doctor will return "Doctor".
     *
     * @return the role name of this person
     */
    public abstract String getRole();

    // ------------------------------------------------------------------------
    // COMMON BUSINESS / HELPER METHODS
    // ------------------------------------------------------------------------

    /**
     * Computes the full name of the person by combining first and last name.
     *
     * @return Formatted full name
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "Unknown";
        }
        String first = (firstName != null) ? firstName.trim() : "";
        String last = (lastName != null) ? lastName.trim() : "";
        return (first + " " + last).trim();
    }

    // ------------------------------------------------------------------------
    // GETTERS AND SETTERS (ENCAPSULATION WITH VALIDATION)
    // ------------------------------------------------------------------------

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = (firstName != null) ? firstName.trim() : "";
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = (lastName != null) ? lastName.trim() : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = (email != null) ? email.trim() : "";
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = (phoneNumber != null) ? phoneNumber.trim() : "";
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = (address != null) ? address.trim() : "";
    }

    // ------------------------------------------------------------------------
    // METHOD OVERRIDING (POLYMORPHISM)
    // ------------------------------------------------------------------------

    /**
     * Default implementation of Displayable interface.
     * Subclasses can override this to provide more specific details.
     */
    @Override
    public String getDisplayInfo() {
        return String.format("[%s #%d] %s | Phone: %s | Email: %s",
                getRole(), id, getFullName(), phoneNumber, email);
    }

    /**
     * Overrides java.lang.Object's toString() method for readable logging/debugging.
     */
    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", role='" + getRole() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person person)) return false;
        return id == person.id && Objects.equals(email, person.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}
