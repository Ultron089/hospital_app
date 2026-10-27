# Hospital Appointment Management System (Phase 1)

A JavaFX desktop application for scheduling medical appointments, managing patient medical histories, and coordinating physician consultation schedules. Built with **Java 21** and **Apache Maven**.

---

## 📋 Phase 1 Scope & Status

Phase 1 establishes the foundational architecture and Object-Oriented design without database coupling:
- [x] Maven project scaffold with Java 21 and OpenJFX 21 dependencies.
- [x] Full multi-package architecture: `model`, `service`, `dao`, `gui`, `exception`, `util`.
- [x] Core domain model classes demonstrating essential Java OOP principles:
  - `Person` (Abstract base class)
  - `Patient` (Concrete subclass)
  - `Doctor` (Concrete subclass)
  - `Appointment` (Composite model with `AppointmentStatus` enum)
- [x] Java interfaces: `Identifiable`, `Displayable`, `GenericDao<T, ID>`, and service contracts.
- [x] Modern JavaFX GUI with navigation placeholders for **Dashboard**, **Patients**, **Doctors**, **Appointments**, and **Reports**.
- [x] Automated unit test suite using JUnit 5.
- [x] *Note*: MySQL persistence and full database business logic are deferred to Phase 2.

---

## 🏗️ Architecture & OOP Concepts Illustrated

This project demonstrates core Java Object-Oriented Programming (OOP) concepts in beginner-friendly ways:

### 1. Abstraction (`Person`, `Identifiable`, `Displayable`)
- `Person` is declared with the `abstract` keyword. You cannot instantiate a generic person with `new Person()`.
- An abstract class defines a common blueprint and forces concrete subclasses to implement abstract methods like `public abstract String getRole()`.
- Interfaces (`Identifiable`, `Displayable`, `GenericDao`) define **what** actions an object can perform without dictating **how**.

### 2. Encapsulation (`Person`, `Patient`, `Doctor`, `Appointment`)
- All instance variables (fields) are `private` to protect data integrity (data hiding).
- External classes interact with fields exclusively via public `getter` and `setter` methods.
- Setters enforce domain validation (e.g., preventing negative consultation fees in `Doctor.setConsultationFee()`).

### 3. Inheritance (`Patient extends Person`, `Doctor extends Person`)
- Both `Patient` and `Doctor` inherit common attributes (id, names, email, phone, address) and behaviors from `Person`.
- Reuses code and enforces an "IS-A" relationship (`Patient IS-A Person`).
- Subclasses use `super(...)` to invoke the parent constructor.

### 4. Polymorphism & Method Overriding (`@Override`)
- `Person` implements `Displayable` with a base `getDisplayInfo()` method.
- `Patient` and `Doctor` override `getRole()` and `getDisplayInfo()` to output role-specific details.
- Code can manipulate `Person` references polymorphically without knowing whether the object is a Patient or a Doctor at compile time.

### 5. Composition / Aggregation (`Appointment HAS-A Patient and Doctor`)
- `Appointment` connects patients and doctors using object references rather than plain string names or integer IDs.

---

## 📁 Package Structure

```
hospital_app/
├── pom.xml                                   # Maven dependencies & build configuration
├── README.md                                 # Documentation and run instructions
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/hospital/
    │   │       ├── Main.java                 # Standard application entry point
    │   │       ├── model/                    # Domain models & interfaces
    │   │       │   ├── Identifiable.java     # Interface for entities with integer IDs
    │   │       │   ├── Displayable.java      # Interface for UI string representations
    │   │       │   ├── Person.java           # Abstract base class
    │   │       │   ├── Patient.java          # Patient subclass
    │   │       │   ├── Doctor.java           # Doctor subclass
    │   │       │   ├── Appointment.java      # Composite appointment entity
    │   │       │   └── AppointmentStatus.java# Enum for appointment life-cycle states
    │   │       ├── dao/                      # Data Access Object contracts
    │   │       │   ├── GenericDao.java       # Generic CRUD interface
    │   │       │   ├── PatientDao.java       # Patient DAO contract
    │   │       │   ├── DoctorDao.java        # Doctor DAO contract
    │   │       │   └── AppointmentDao.java   # Appointment DAO contract
    │   │       ├── service/                  # Business logic contracts & mock data
    │   │       │   ├── PatientService.java   # Patient service contract
    │   │       │   ├── DoctorService.java    # Doctor service contract
    │   │       │   ├── AppointmentService.java# Appointment service contract
    │   │       │   └── MockDataService.java  # In-memory sample data provider
    │   │       ├── gui/                      # Presentation layer
    │   │       │   └── HospitalApp.java      # JavaFX main application & navigation
    │   │       ├── exception/                # Custom exception hierarchy
    │   │       │   ├── HospitalException.java# Base checked exception
    │   │       │   ├── ValidationException.java
    │   │       │   └── ResourceNotFoundException.java
    │   │       └── util/                     # Helper utilities
    │   │           ├── AppConstants.java     # System constants
    │   │           ├── DateTimeUtil.java     # Date/time formatting helpers
    │   │           └── ValidationUtil.java   # Email/phone input validators
    │   └── resources/
    │       └── css/
    │           └── style.css                 # Modern stylesheet for JavaFX UI
    └── test/
        └── java/
            └── com/hospital/model/
                └── ModelTest.java            # JUnit 5 unit tests for OOP models
```

---

## 🚀 How to Run the Application on Ubuntu

### 1. Prerequisites
Ensure Java 21 and Maven are installed on Ubuntu:
```bash
sudo apt update
sudo apt install -y openjdk-21-jdk maven
```

Verify installations:
```bash
java -version
mvn -version
```

### 2. Compile the Project & Run Unit Tests
Inside the project root directory (`/home/atharv/hospital_app`):
```bash
mvn clean test
```
*Expected result*: `BUILD SUCCESS` with 4 unit tests passing.

### 3. Launch the JavaFX Application
To launch the JavaFX GUI window directly via the Maven JavaFX plugin:
```bash
mvn javafx:run
```

#### Running in a Headless / Remote SSH Environment
If you are connected via an SSH session without an active X11 display session (`$DISPLAY`), you can either:
- Enable X11 forwarding: `ssh -X user@host` and run `mvn javafx:run`, or
- Use a virtual framebuffer:
  ```bash
  sudo apt install -y xvfb
  xvfb-run mvn javafx:run
  ```

---

## 🔮 Upcoming Phases
- **Phase 2**: MySQL schema creation, JDBC connection manager, DAO implementations with prepared statements, and transactional appointment booking.
- **Phase 3**: Dynamic search filters, input validation modals, reports generation, and PDF export.
