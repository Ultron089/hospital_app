package com.hospital.gui;

import com.hospital.exception.HospitalException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.MockDataService;
import com.hospital.util.AppConstants;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * ============================================================================
 * JAVAFX GUI APPLICATION: MAIN WINDOW & NAVIGATION CONTROLLER
 * ============================================================================
 * JavaFX Architecture Core Concepts:
 * 1. Application: The base class that every JavaFX GUI app must extend.
 * 2. Stage: The top-level window (analogous to the browser window or OS
 * window).
 * 3. Scene: The container for all visual nodes displayed within the Stage.
 * 4. Scene Graph / Layouts: A tree of nodes (BorderPane, VBox, HBox) organizing
 * UI elements.
 * 5. Event Handling: Java 8+ lambdas (e -> handleAction()) for interactive
 * button clicks.
 * ============================================================================
 */
public class HospitalApp extends Application {

    private MockDataService mockDataService;
    private String databaseErrorMessage = null;

    private BorderPane rootLayout;
    private Button btnNavDashboard;
    private Button btnNavPatients;
    private Button btnNavDoctors;
    private Button btnNavAppointments;
    private Button btnNavReports;

    private Label lblStatusTimestamp;

    public HospitalApp() {
        try {
            this.mockDataService = new MockDataService();
        } catch (Exception e) {
            this.databaseErrorMessage = e.getMessage() != null ? e.getMessage() : "Database connection failed.";
            this.mockDataService = null;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(AppConstants.APP_NAME + " - " + AppConstants.APP_VERSION);

        // Root Layout container
        rootLayout = new BorderPane();

        // 1. Top Bar (Header)
        rootLayout.setTop(createHeader());

        // 2. Left Sidebar (Navigation)
        rootLayout.setLeft(createSidebar());

        // 3. Center Content (Defaults to Dashboard if connected, or Database Error View if failed)
        if (databaseErrorMessage != null) {
            rootLayout.setCenter(createDatabaseErrorView(databaseErrorMessage));
        } else {
            rootLayout.setCenter(createDashboardView());
        }

        // 4. Bottom Bar (Status Bar)
        rootLayout.setBottom(createFooter());

        // Set up Scene with responsive minimum size
        Scene scene = new Scene(rootLayout, 1100, 720);

        // Load custom CSS stylesheet
        try {
            String cssPath = getClass().getResource("/css/style.css") != null
                    ? getClass().getResource("/css/style.css").toExternalForm()
                    : null;
            if (cssPath != null) {
                scene.getStylesheets().add(cssPath);
            }
        } catch (Exception ignored) {
            // Fallback gracefully to default styles if CSS resource lookup differs
        }

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        primaryStage.show();

        if (databaseErrorMessage != null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Connection Error");
            alert.setHeaderText("Failed to Connect to MySQL Database");
            alert.setContentText(databaseErrorMessage + "\n\nIn-memory fallback is disabled to protect data integrity.");
            alert.show();
        }
    }

    // ========================================================================
    // HEADER COMPONENT
    // ========================================================================
    private Node createHeader() {
        HBox header = new HBox(16);
        header.getStyleClass().add("header-bar");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("🏥 " + AppConstants.HOSPITAL_NAME);
        lblTitle.getStyleClass().add("header-title");

        Label lblSubtitle = new Label(AppConstants.APP_NAME + " | " + AppConstants.TAGLINE);
        lblSubtitle.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(lblTitle, lblSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblBadge = new Label();
        if (databaseErrorMessage != null) {
            lblBadge.setText("● MySQL Offline (Error)");
            lblBadge.getStyleClass().add("status-badge-offline");
        } else if (com.hospital.util.DbConnectionManager.isConfigured()) {
            lblBadge.setText("● MySQL Connected (hospital_db)");
            lblBadge.getStyleClass().add("status-badge-online");
        } else {
            lblBadge.setText("● In-Memory Mode (Active)");
            lblBadge.getStyleClass().add("status-badge-online");
        }

        header.getChildren().addAll(titleBox, spacer, lblBadge);
        return header;
    }

    // ========================================================================
    // SIDEBAR NAVIGATION
    // ========================================================================
    private Node createSidebar() {
        VBox sidebar = new VBox(6);
        sidebar.getStyleClass().add("sidebar");

        Label lblSection = new Label("MAIN NAVIGATION");
        lblSection.getStyleClass().add("sidebar-heading");

        btnNavDashboard = createNavButton("📊  Dashboard", () -> {
            if (databaseErrorMessage != null) {
                showView(createDatabaseErrorView(databaseErrorMessage), btnNavDashboard);
            } else {
                showView(createDashboardView(), btnNavDashboard);
            }
        });
        btnNavPatients = createNavButton("👤  Patients", () -> {
            if (databaseErrorMessage != null) {
                showView(createDatabaseErrorView(databaseErrorMessage), btnNavPatients);
            } else {
                showView(createPatientsView(), btnNavPatients);
            }
        });
        btnNavDoctors = createNavButton("🩺  Doctors", () -> {
            if (databaseErrorMessage != null) {
                showView(createDatabaseErrorView(databaseErrorMessage), btnNavDoctors);
            } else {
                showView(createDoctorsView(), btnNavDoctors);
            }
        });
        btnNavAppointments = createNavButton("📅  Appointments", () -> {
            if (databaseErrorMessage != null) {
                showView(createDatabaseErrorView(databaseErrorMessage), btnNavAppointments);
            } else {
                showView(createAppointmentsView(), btnNavAppointments);
            }
        });
        btnNavReports = createNavButton("📈  Reports", () -> {
            if (databaseErrorMessage != null) {
                showView(createDatabaseErrorView(databaseErrorMessage), btnNavReports);
            } else {
                showView(createReportsView(), btnNavReports);
            }
        });

        // Mark Dashboard active by default
        btnNavDashboard.getStyleClass().add("nav-button-active");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Sidebar user info badge at bottom
        VBox userInfo = new VBox(4);
        userInfo.setStyle("-fx-background-color: #0f172a; -fx-padding: 12px; -fx-background-radius: 8px;");
        Label lblRole = new Label("Signed in as Admin");
        lblRole.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        Label lblUser = new Label("System Administrator");
        lblUser.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px;");
        userInfo.getChildren().addAll(lblRole, lblUser);

        sidebar.getChildren().addAll(
                lblSection,
                btnNavDashboard,
                btnNavPatients,
                btnNavDoctors,
                btnNavAppointments,
                btnNavReports,
                spacer,
                userInfo);

        return sidebar;
    }

    private Button createNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void showView(Node viewNode, Button activeButton) {
        rootLayout.setCenter(viewNode);

        // Reset active style on all nav buttons
        List.of(btnNavDashboard, btnNavPatients, btnNavDoctors, btnNavAppointments, btnNavReports)
                .forEach(b -> b.getStyleClass().remove("nav-button-active"));

        // Highlight selected nav button
        if (!activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }

        updateStatusTime();
    }

    // ========================================================================
    // VIEW 1: DASHBOARD VIEW
    // ========================================================================
    private Node createDashboardView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("content-area");

        // Page Header
        VBox headerBox = new VBox(4);
        Label lblHeading = new Label("Operational Overview");
        lblHeading.getStyleClass().add("page-title");
        Label lblDesc = new Label("Real-time summary of hospital capacity, appointments, and care providers.");
        lblDesc.getStyleClass().add("page-description");
        headerBox.getChildren().addAll(lblHeading, lblDesc);

        String sysStatus = com.hospital.util.DbConnectionManager.isConfigured() ? "Connected" : "Active";
        String sysDesc = com.hospital.util.DbConnectionManager.isConfigured() ? "MySQL hospital_db" : "In-Memory Storage";

        // Key Metric Stat Cards
        HBox statsGrid = new HBox(16);
        statsGrid.getChildren().addAll(
                createStatCard("TOTAL PATIENTS", String.valueOf(mockDataService.getPatients().size()),
                        "Active Profiles"),
                createStatCard("DOCTORS ON DUTY", String.valueOf(mockDataService.getDoctors().size()),
                        "Across 4 Specialties"),
                createStatCard("TOTAL APPOINTMENTS", String.valueOf(mockDataService.getAppointments().size()),
                        "Booked Sessions"),
                createStatCard("SYSTEM STATUS", sysStatus, sysDesc));

        // Phase 1 Architecture Notice
        VBox notice = new VBox(6);
        notice.getStyleClass().add("notice-box");
        Label lblNoticeTitle = new Label("ℹ️ Phase 1 Milestones Accomplished:");
        lblNoticeTitle.getStyleClass().add("notice-box-title");
        Label lblNoticeBody = new Label(
                "• Java 21 & Maven Project Architecture established.\n" +
                        "• OOP Models: Person (Abstract), Patient, Doctor, Appointment, AppointmentStatus (Enum).\n" +
                        "• Core Interfaces: Identifiable, Displayable, GenericDao, and Service contracts defined.\n" +
                        "• Clean Separation of Concerns: model, service, dao, gui, exception, and util packages.\n" +
                        "• Ready for Phase 2: MySQL persistence and JDBC connection pool integration.");
        lblNoticeBody.getStyleClass().add("notice-box-text");
        notice.getChildren().addAll(lblNoticeTitle, lblNoticeBody);

        // Quick Overview Lists
        Label lblRecent = new Label("Upcoming Scheduled Appointments (Preview)");
        lblRecent.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        VBox appointmentList = new VBox(10);
        for (Appointment apt : mockDataService.getAppointments()) {
            appointmentList.getChildren().add(createAppointmentCard(apt));
        }

        ScrollPane scrollPane = new ScrollPane(appointmentList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        view.getChildren().addAll(headerBox, statsGrid, notice, lblRecent, scrollPane);
        return wrapInScroll(view);
    }

    // ========================================================================
    // VIEW 2: PATIENTS NAVIGATION PLACEHOLDER
    // ========================================================================
    private Node createPatientsView() {
        VBox view = new VBox(18);
        view.getStyleClass().add("content-area");

        // Header + Action Bar
        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(4);
        Label lblHeading = new Label("Patients Directory");
        lblHeading.getStyleClass().add("page-title");
        Label lblDesc = new Label("Manage patient electronic records, emergency contacts, and medical histories.");
        lblDesc.getStyleClass().add("page-description");
        titles.getChildren().addAll(lblHeading, lblDesc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNewPatient = new Button("➕ Register Patient");
        btnNewPatient.getStyleClass().add("action-button");

        // Patients List Cards
        VBox cardsList = new VBox(10);
        refreshPatientCards(cardsList);

        btnNewPatient.setOnAction(e -> openRegisterPatientDialog(cardsList));

        titleRow.getChildren().addAll(titles, spacer, btnNewPatient);

        // Search Field
        TextField txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Search patients by name, phone, email, ID, or blood group...");
        txtSearch.getStyleClass().add("search-field");
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            cardsList.getChildren().clear();
            List<Patient> results = mockDataService.getPatientService().searchPatients(newVal);
            for (Patient p : results) {
                cardsList.getChildren().add(createPatientCard(p));
            }
        });

        // Architecture Note
        VBox notice = createArchitectureBanner("Patient Model (Inherits Person)",
                "Demonstrates Inheritance ('Patient extends Person'), Encapsulation (private fields), and Polymorphism.");

        view.getChildren().addAll(titleRow, notice, txtSearch, cardsList);
        return wrapInScroll(view);
    }

    private void refreshPatientCards(VBox cardsList) {
        cardsList.getChildren().clear();
        for (Patient patient : mockDataService.getPatients()) {
            cardsList.getChildren().add(createPatientCard(patient));
        }
    }

    private void openRegisterPatientDialog(VBox cardsList) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register New Patient");
        dialog.setHeaderText("Enter Patient Information");

        ButtonType btnRegister = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnRegister, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 24, 10, 24));

        TextField txtFirstName = new TextField();
        txtFirstName.setPromptText("e.g. John");

        TextField txtLastName = new TextField();
        txtLastName.setPromptText("e.g. Doe");

        TextField txtPhone = new TextField();
        txtPhone.setPromptText("e.g. +1-555-0123");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("e.g. john.doe@example.com");

        DatePicker dpDob = new DatePicker();
        dpDob.setValue(LocalDate.of(1995, 1, 1));
        dpDob.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cmbBlood = new ComboBox<>();
        cmbBlood.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown");
        cmbBlood.setValue("A+");
        cmbBlood.setMaxWidth(Double.MAX_VALUE);

        TextField txtAddress = new TextField();
        txtAddress.setPromptText("e.g. 123 Elm Street, City");

        TextField txtHistory = new TextField();
        txtHistory.setPromptText("e.g. Asthma, Penicillin Allergy (or 'None')");

        TextField txtEmergency = new TextField();
        txtEmergency.setPromptText("e.g. Jane Doe (+1-555-0124)");

        grid.add(new Label("First Name: *"), 0, 0);
        grid.add(txtFirstName, 1, 0);

        grid.add(new Label("Last Name: *"), 0, 1);
        grid.add(txtLastName, 1, 1);

        grid.add(new Label("Phone Number: *"), 0, 2);
        grid.add(txtPhone, 1, 2);

        grid.add(new Label("Email Address:"), 0, 3);
        grid.add(txtEmail, 1, 3);

        grid.add(new Label("Date of Birth: *"), 0, 4);
        grid.add(dpDob, 1, 4);

        grid.add(new Label("Blood Group:"), 0, 5);
        grid.add(cmbBlood, 1, 5);

        grid.add(new Label("Address:"), 0, 6);
        grid.add(txtAddress, 1, 6);

        grid.add(new Label("Medical History:"), 0, 7);
        grid.add(txtHistory, 1, 7);

        grid.add(new Label("Emergency Contact:"), 0, 8);
        grid.add(txtEmergency, 1, 8);

        dialog.getDialogPane().setContent(grid);

        if (rootLayout.getScene() != null && !rootLayout.getScene().getStylesheets().isEmpty()) {
            dialog.getDialogPane().getStylesheets().addAll(rootLayout.getScene().getStylesheets());
        }

        Button registerButton = (Button) dialog.getDialogPane().lookupButton(btnRegister);
        registerButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                String first = txtFirstName.getText() != null ? txtFirstName.getText().trim() : "";
                String last = txtLastName.getText() != null ? txtLastName.getText().trim() : "";
                String phone = txtPhone.getText() != null ? txtPhone.getText().trim() : "";
                String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";
                LocalDate dob = dpDob.getValue();
                String blood = cmbBlood.getValue();
                String address = txtAddress.getText() != null ? txtAddress.getText().trim() : "";
                String history = txtHistory.getText() != null ? txtHistory.getText().trim() : "";
                String emergency = txtEmergency.getText() != null ? txtEmergency.getText().trim() : "";

                Patient newPatient = new Patient(
                        0,
                        first,
                        last,
                        email,
                        phone,
                        address,
                        dob,
                        blood,
                        history,
                        emergency
                );

                Patient registered = mockDataService.getPatientService().registerPatient(newPatient);

                refreshPatientCards(cardsList);

                showSuccessAlert("Patient Registered",
                        String.format("Patient %s has been registered successfully!\nGenerated Patient ID: #%d",
                                registered.getFullName(), registered.getId()));

            } catch (ValidationException ex) {
                event.consume();
                showErrorAlert("Validation Error", ex.getMessage());
            } catch (HospitalException ex) {
                event.consume();
                showErrorAlert("Registration Error", ex.getMessage());
            } catch (Exception ex) {
                event.consume();
                showErrorAlert("Registration Error", "Failed to register patient: " + ex.getMessage());
            }
        });

        dialog.showAndWait();
    }

    // ========================================================================
    // VIEW 3: DOCTORS NAVIGATION PLACEHOLDER
    // ========================================================================
    private Node createDoctorsView() {
        VBox view = new VBox(18);
        view.getStyleClass().add("content-area");

        // Header + Action Bar
        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(4);
        Label lblHeading = new Label("Medical Specialists & Doctors");
        lblHeading.getStyleClass().add("page-title");
        Label lblDesc = new Label("View physician departments, active licenses, consultation fees, and availability.");
        lblDesc.getStyleClass().add("page-description");
        titles.getChildren().addAll(lblHeading, lblDesc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNewDoctor = new Button("➕ Add Doctor");
        btnNewDoctor.getStyleClass().add("action-button");

        // Doctors List Cards
        VBox cardsList = new VBox(10);
        refreshDoctorCards(cardsList);

        btnNewDoctor.setOnAction(e -> openAddDoctorDialog(cardsList));

        titleRow.getChildren().addAll(titles, spacer, btnNewDoctor);

        // Search Field
        TextField txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Search doctors by name, specialization, department, or license...");
        txtSearch.getStyleClass().add("search-field");
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            cardsList.getChildren().clear();
            List<Doctor> results = mockDataService.getDoctorService().searchDoctors(newVal);
            for (Doctor d : results) {
                cardsList.getChildren().add(createDoctorCard(d));
            }
        });

        // Architecture Note
        VBox notice = createArchitectureBanner("Doctor Model (Inherits Person)",
                "Demonstrates Inheritance ('Doctor extends Person'), Overriding ('getRole()', 'getDisplayInfo()'), and fee validation.");

        view.getChildren().addAll(titleRow, notice, txtSearch, cardsList);
        return wrapInScroll(view);
    }

    private void refreshDoctorCards(VBox cardsList) {
        cardsList.getChildren().clear();
        for (Doctor doctor : mockDataService.getDoctors()) {
            cardsList.getChildren().add(createDoctorCard(doctor));
        }
    }

    private void openAddDoctorDialog(VBox cardsList) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Doctor");
        dialog.setHeaderText("Enter Physician Information & Credentials");

        ButtonType btnAdd = new ButtonType("Add Doctor", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAdd, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 24, 10, 24));

        TextField txtFirstName = new TextField();
        txtFirstName.setPromptText("e.g. Robert");

        TextField txtLastName = new TextField();
        txtLastName.setPromptText("e.g. Lang");

        TextField txtLicense = new TextField();
        txtLicense.setPromptText("e.g. MD-NY-55102");

        ComboBox<String> cmbSpec = new ComboBox<>();
        cmbSpec.setEditable(true);
        Set<String> existingSpecs = mockDataService.getDoctorService().getAllSpecializations();
        if (existingSpecs.isEmpty()) {
            cmbSpec.getItems().addAll("Cardiology", "Pediatrics", "Neurology", "Orthopedics", "General Medicine");
        } else {
            cmbSpec.getItems().addAll(existingSpecs);
        }
        cmbSpec.setValue("General Medicine");
        cmbSpec.setMaxWidth(Double.MAX_VALUE);

        TextField txtDept = new TextField("General OPD");
        TextField txtFee = new TextField("120.00");

        TextField txtPhone = new TextField();
        txtPhone.setPromptText("e.g. +1-555-0210");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("e.g. r.lang@hospital.org");

        TextField txtAddress = new TextField();
        txtAddress.setPromptText("e.g. Hospital Suites #210");

        grid.add(new Label("First Name: *"), 0, 0);
        grid.add(txtFirstName, 1, 0);

        grid.add(new Label("Last Name: *"), 0, 1);
        grid.add(txtLastName, 1, 1);

        grid.add(new Label("Medical License #: *"), 0, 2);
        grid.add(txtLicense, 1, 2);

        grid.add(new Label("Specialization: *"), 0, 3);
        grid.add(cmbSpec, 1, 3);

        grid.add(new Label("Department: *"), 0, 4);
        grid.add(txtDept, 1, 4);

        grid.add(new Label("Consultation Fee ($): *"), 0, 5);
        grid.add(txtFee, 1, 5);

        grid.add(new Label("Phone Number:"), 0, 6);
        grid.add(txtPhone, 1, 6);

        grid.add(new Label("Email Address:"), 0, 7);
        grid.add(txtEmail, 1, 7);

        grid.add(new Label("Office / Suite Address:"), 0, 8);
        grid.add(txtAddress, 1, 8);

        dialog.getDialogPane().setContent(grid);

        if (rootLayout.getScene() != null && !rootLayout.getScene().getStylesheets().isEmpty()) {
            dialog.getDialogPane().getStylesheets().addAll(rootLayout.getScene().getStylesheets());
        }

        Button addButton = (Button) dialog.getDialogPane().lookupButton(btnAdd);
        addButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                String first = txtFirstName.getText() != null ? txtFirstName.getText().trim() : "";
                String last = txtLastName.getText() != null ? txtLastName.getText().trim() : "";
                String license = txtLicense.getText() != null ? txtLicense.getText().trim() : "";
                String spec = cmbSpec.getValue() != null ? cmbSpec.getValue().trim() : "";
                String dept = txtDept.getText() != null ? txtDept.getText().trim() : "";
                String phone = txtPhone.getText() != null ? txtPhone.getText().trim() : "";
                String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";
                String address = txtAddress.getText() != null ? txtAddress.getText().trim() : "";

                double fee;
                try {
                    fee = Double.parseDouble(txtFee.getText().trim());
                } catch (NumberFormatException nfe) {
                    throw new ValidationException("consultationFee", "Consultation fee must be a valid positive number.");
                }
                if (fee <= 0.0) {
                    throw new ValidationException("consultationFee", "Consultation fee must be greater than zero.");
                }

                Doctor newDoctor = new Doctor(
                        0,
                        first,
                        last,
                        email,
                        phone,
                        address,
                        spec,
                        license,
                        fee,
                        dept
                );

                Doctor registered = mockDataService.getDoctorService().registerDoctor(newDoctor);

                refreshDoctorCards(cardsList);

                showSuccessAlert("Doctor Added",
                        String.format("Dr. %s has been registered successfully!\nGenerated Doctor ID: #%d\nSpecialization: %s\nLicense #: %s",
                                registered.getFullName(), registered.getId(), registered.getSpecialization(), registered.getLicenseNumber()));

            } catch (ValidationException ex) {
                event.consume();
                showErrorAlert("Validation Error", ex.getMessage());
            } catch (HospitalException ex) {
                event.consume();
                showErrorAlert("Registration Error", ex.getMessage());
            } catch (Exception ex) {
                event.consume();
                showErrorAlert("Registration Error", "Failed to add doctor: " + ex.getMessage());
            }
        });

        dialog.showAndWait();
    }

    // ========================================================================
    // VIEW 4: APPOINTMENTS NAVIGATION PLACEHOLDER
    // ========================================================================
    private Node createAppointmentsView() {
        VBox view = new VBox(18);
        view.getStyleClass().add("content-area");

        // Header + Action Bar
        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(4);
        Label lblHeading = new Label("Appointment Bookings");
        lblHeading.getStyleClass().add("page-title");
        Label lblDesc = new Label("Schedule patient consultations, coordinate doctor availability, and update status.");
        lblDesc.getStyleClass().add("page-description");
        titles.getChildren().addAll(lblHeading, lblDesc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnBook = new Button("➕ Book Appointment");
        btnBook.getStyleClass().add("action-button");

        // Appointments List Cards
        VBox cardsList = new VBox(10);
        refreshAppointmentCards(cardsList);

        btnBook.setOnAction(e -> openBookAppointmentDialog(cardsList));

        titleRow.getChildren().addAll(titles, spacer, btnBook);

        // Architecture Note
        VBox notice = createArchitectureBanner("Appointment Model (Composition Pattern)",
                "Demonstrates Composition: Appointment HAS-A Patient and HAS-A Doctor, linked with AppointmentStatus Enum.");

        view.getChildren().addAll(titleRow, notice, cardsList);
        return wrapInScroll(view);
    }

    private void refreshAppointmentCards(VBox cardsList) {
        cardsList.getChildren().clear();
        for (Appointment apt : mockDataService.getAppointments()) {
            cardsList.getChildren().add(createAppointmentCard(apt, cardsList));
        }
    }

    private void openBookAppointmentDialog(VBox cardsList) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Book Appointment");
        dialog.setHeaderText("Schedule a Patient Consultation");

        ButtonType btnBook = new ButtonType("Confirm Booking", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnBook, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 24, 10, 24));

        ComboBox<Patient> cmbPatient = new ComboBox<>();
        cmbPatient.getItems().addAll(mockDataService.getPatients());
        cmbPatient.setMaxWidth(Double.MAX_VALUE);
        cmbPatient.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Patient p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : "[#" + p.getId() + "] " + p.getFullName() + " (" + p.getPhoneNumber() + ")");
            }
        });
        cmbPatient.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Patient p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "Select Patient..." : "[#" + p.getId() + "] " + p.getFullName());
            }
        });
        if (!mockDataService.getPatients().isEmpty()) {
            cmbPatient.setValue(mockDataService.getPatients().get(0));
        }

        ComboBox<Doctor> cmbDoctor = new ComboBox<>();
        cmbDoctor.getItems().addAll(mockDataService.getDoctors());
        cmbDoctor.setMaxWidth(Double.MAX_VALUE);
        cmbDoctor.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Doctor d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "" : "[#" + d.getId() + "] Dr. " + d.getFullName() + " - " + d.getSpecialization() + " ($" + String.format("%.2f", d.getConsultationFee()) + ")");
            }
        });
        cmbDoctor.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Doctor d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "Select Doctor..." : "[#" + d.getId() + "] Dr. " + d.getFullName() + " (" + d.getSpecialization() + ")");
            }
        });
        if (!mockDataService.getDoctors().isEmpty()) {
            cmbDoctor.setValue(mockDataService.getDoctors().get(0));
        }

        DatePicker dpDate = new DatePicker(LocalDate.now().plusDays(1));
        dpDate.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cmbTime = new ComboBox<>();
        cmbTime.getItems().addAll(
                "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"
        );
        cmbTime.setValue("10:00");
        cmbTime.setMaxWidth(Double.MAX_VALUE);

        TextField txtReason = new TextField();
        txtReason.setPromptText("e.g. Annual Checkup, Follow-up, Fever");

        grid.add(new Label("Select Patient: *"), 0, 0);
        grid.add(cmbPatient, 1, 0);

        grid.add(new Label("Select Doctor: *"), 0, 1);
        grid.add(cmbDoctor, 1, 1);

        grid.add(new Label("Appointment Date: *"), 0, 2);
        grid.add(dpDate, 1, 2);

        grid.add(new Label("Appointment Time: *"), 0, 3);
        grid.add(cmbTime, 1, 3);

        grid.add(new Label("Reason for Visit: *"), 0, 4);
        grid.add(txtReason, 1, 4);

        dialog.getDialogPane().setContent(grid);

        if (rootLayout.getScene() != null && !rootLayout.getScene().getStylesheets().isEmpty()) {
            dialog.getDialogPane().getStylesheets().addAll(rootLayout.getScene().getStylesheets());
        }

        Button bookButton = (Button) dialog.getDialogPane().lookupButton(btnBook);
        bookButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Patient patient = cmbPatient.getValue();
                Doctor doctor = cmbDoctor.getValue();
                LocalDate date = dpDate.getValue();
                String timeStr = cmbTime.getValue();
                String reason = txtReason.getText() != null ? txtReason.getText().trim() : "";

                if (patient == null) throw new ValidationException("patient", "Please select a patient.");
                if (doctor == null) throw new ValidationException("doctor", "Please select a doctor.");
                if (date == null) throw new ValidationException("date", "Please select an appointment date.");
                if (timeStr == null || timeStr.trim().isEmpty()) throw new ValidationException("time", "Please select a time slot.");

                LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                LocalDateTime dateTime = LocalDateTime.of(date, time);

                Appointment booked = mockDataService.getAppointmentService().bookAppointment(
                        patient.getId(), doctor.getId(), dateTime, reason
                );

                refreshAppointmentCards(cardsList);

                showSuccessAlert("Appointment Booked",
                        String.format("Appointment #%d booked successfully!\nPatient: %s\nDoctor: Dr. %s\nDate & Time: %s",
                                booked.getId(), booked.getPatient().getFullName(),
                                booked.getDoctor().getFullName(), booked.getFormattedDateTime()));

            } catch (ValidationException ex) {
                event.consume();
                showErrorAlert("Booking Failed", ex.getMessage());
            } catch (HospitalException ex) {
                event.consume();
                showErrorAlert("Booking Error", ex.getMessage());
            } catch (Exception ex) {
                event.consume();
                showErrorAlert("Error", "Failed to book appointment: " + ex.getMessage());
            }
        });

        dialog.showAndWait();
    }

    // ========================================================================
    // VIEW 5: REPORTS NAVIGATION PLACEHOLDER
    // ========================================================================
    private Node createReportsView() {
        VBox view = new VBox(18);
        view.getStyleClass().add("content-area");

        // Header
        VBox titles = new VBox(4);
        Label lblHeading = new Label("Hospital Reports & Analytics");
        lblHeading.getStyleClass().add("page-title");
        Label lblDesc = new Label(
                "Aggregated hospital performance indicators, patient visit trends, and billing summaries.");
        lblDesc.getStyleClass().add("page-description");
        titles.getChildren().addAll(lblHeading, lblDesc);

        // Notice
        VBox notice = createArchitectureBanner("Phase 3 Analytics Module Placeholder",
                "This module will generate scheduled reports, revenue summaries, and doctor performance metrics from MySQL data.");

        // Report Cards
        VBox reportsGrid = new VBox(12);

        reportsGrid.getChildren().addAll(
                createReportPlaceholderCard("📊 Daily Consultation Summary",
                        "Breakdown of scheduled, completed, and cancelled appointments for any selected date range.",
                        "Status: Configured for Phase 3 SQL Aggregations"),
                createReportPlaceholderCard("🩺 Doctor Workload & Utilization",
                        "Consultation volume per doctor, average session duration, and departmental distribution.",
                        "Status: Service Layer Ready"),
                createReportPlaceholderCard("💰 Revenue & Billing Reports",
                        "Summary of consultation fees billed and patient payment reconciliations.",
                        "Status: Service Layer Ready"),
                createReportPlaceholderCard("📁 Audit & Compliance Records",
                        "Historical log of access and appointment modifications.",
                        "Status: Service Layer Ready"));

        view.getChildren().addAll(titles, notice, reportsGrid);
        return wrapInScroll(view);
    }

    // ========================================================================
    // FOOTER (STATUS BAR)
    // ========================================================================
    private Node createFooter() {
        HBox footer = new HBox(20);
        footer.getStyleClass().add("footer-bar");
        footer.setAlignment(Pos.CENTER_LEFT);

        String persistenceInfo;
        if (databaseErrorMessage != null) {
            persistenceInfo = "Persistence: MySQL Connection Failed";
        } else if (com.hospital.util.DbConnectionManager.isConfigured()) {
            persistenceInfo = "Persistence: MySQL (hospital_db)";
        } else {
            persistenceInfo = "Persistence: In-Memory Storage";
        }
        Label lblInfo = new Label(AppConstants.APP_NAME + " | " + persistenceInfo + " | Java 21 | OpenJFX 21");
        lblInfo.getStyleClass().add("footer-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblStatusTimestamp = new Label();
        lblStatusTimestamp.getStyleClass().add("footer-text");
        updateStatusTime();

        footer.getChildren().addAll(lblInfo, spacer, lblStatusTimestamp);
        return footer;
    }

    private Node createDatabaseErrorView(String errorMsg) {
        VBox view = new VBox(20);
        view.getStyleClass().add("content-area");

        VBox headerBox = new VBox(6);
        Label lblHeading = new Label("⚠️ Database Connection Failure");
        lblHeading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
        Label lblDesc = new Label("The application is configured to connect to MySQL via db.properties, but could not establish a connection.");
        lblDesc.getStyleClass().add("page-description");
        headerBox.getChildren().addAll(lblHeading, lblDesc);

        VBox errorCard = new VBox(12);
        errorCard.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fca5a5; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 20px;");

        Label lblErrTitle = new Label("Error Details:");
        lblErrTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #991b1b;");

        Label lblErrContent = new Label(errorMsg != null ? errorMsg : "Connection could not be established.");
        lblErrContent.setWrapText(true);
        lblErrContent.setStyle("-fx-font-family: monospace; -fx-text-fill: #7f1d1d; -fx-font-size: 13px;");

        Label lblAdviceTitle = new Label("Troubleshooting Steps:");
        lblAdviceTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-padding: 10px 0 0 0;");

        Label lblAdvice = new Label(
                "1. Verify that MySQL Server is running (e.g. `sudo systemctl status mysql`).\n" +
                "2. Ensure database host, port, and credentials in db.properties are correct.\n" +
                "3. Ensure the database 'hospital_db' exists and the user has required permissions.\n" +
                "4. Note: Automatic fallback to in-memory storage is disabled to protect your data from being lost."
        );
        lblAdvice.setStyle("-fx-text-fill: #475569; -fx-line-spacing: 4px;");

        errorCard.getChildren().addAll(lblErrTitle, lblErrContent, lblAdviceTitle, lblAdvice);
        view.getChildren().addAll(headerBox, errorCard);
        return wrapInScroll(view);
    }

    private void updateStatusTime() {
        if (lblStatusTimestamp != null) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"));
            lblStatusTimestamp.setText("Ready • " + time);
        }
    }

    // ========================================================================
    // UI HELPER CARDS
    // ========================================================================

    private VBox createStatCard(String title, String value, String subtitle) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("stat-title");

        Label lblVal = new Label(value);
        lblVal.getStyleClass().add("stat-value");

        Label lblSub = new Label(subtitle);
        lblSub.getStyleClass().add("stat-subtitle");

        card.getChildren().addAll(lblTitle, lblVal, lblSub);
        return card;
    }

    private VBox createArchitectureBanner(String title, String text) {
        VBox box = new VBox(4);
        box.getStyleClass().add("notice-box");
        Label lblT = new Label("💡 Architecture Note: " + title);
        lblT.getStyleClass().add("notice-box-title");
        Label lblC = new Label(text);
        lblC.getStyleClass().add("notice-box-text");
        box.getChildren().addAll(lblT, lblC);
        return box;
    }

    private HBox createPatientCard(Patient p) {
        HBox card = new HBox(16);
        card.getStyleClass().add("item-card");
        card.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 24px; -fx-padding: 4px;");

        VBox details = new VBox(4);
        Label name = new Label(p.getFullName() + " (ID: #" + p.getId() + ")");
        name.getStyleClass().add("item-card-title");

        Label sub = new Label("Age: " + p.getAge() + " yrs  |  Blood Group: " + p.getBloodGroup() +
                "  |  Phone: " + p.getPhoneNumber() + "  |  Email: " + p.getEmail());
        sub.getStyleClass().add("item-card-detail");

        Label hist = new Label(
                "Medical History: " + p.getMedicalHistory() + "  |  Emergency Contact: " + p.getEmergencyContact());
        hist.getStyleClass().add("item-card-meta");

        details.getChildren().addAll(name, sub, hist);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDetails = new Button("View Record");
        btnDetails.getStyleClass().add("secondary-button");
        btnDetails.setOnAction(e -> showInfoAlert("Patient Details", p.getDisplayInfo()));

        card.getChildren().addAll(avatar, details, spacer, btnDetails);
        return card;
    }

    private HBox createDoctorCard(Doctor d) {
        HBox card = new HBox(16);
        card.getStyleClass().add("item-card");
        card.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label("🩺");
        avatar.setStyle("-fx-font-size: 24px; -fx-padding: 4px;");

        VBox details = new VBox(4);
        Label name = new Label("Dr. " + d.getFullName() + " (ID: #" + d.getId() + ")");
        name.getStyleClass().add("item-card-title");

        Label sub = new Label("Specialization: " + d.getSpecialization() + "  |  Department: " + d.getDepartment() +
                "  |  Consultation Fee: $" + String.format("%.2f", d.getConsultationFee()));
        sub.getStyleClass().add("item-card-detail");

        Label meta = new Label("License: " + d.getLicenseNumber() + "  |  Phone: " + d.getPhoneNumber() + "  |  Email: "
                + d.getEmail());
        meta.getStyleClass().add("item-card-meta");

        details.getChildren().addAll(name, sub, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDetails = new Button("View Profile");
        btnDetails.getStyleClass().add("secondary-button");
        btnDetails.setOnAction(e -> showInfoAlert("Doctor Profile", d.getDisplayInfo()));

        card.getChildren().addAll(avatar, details, spacer, btnDetails);
        return card;
    }

    private HBox createAppointmentCard(Appointment apt) {
        return createAppointmentCard(apt, null);
    }

    private HBox createAppointmentCard(Appointment apt, VBox cardsList) {
        HBox card = new HBox(16);
        card.getStyleClass().add("item-card");
        card.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("📅");
        icon.setStyle("-fx-font-size: 24px; -fx-padding: 4px;");

        VBox details = new VBox(4);
        Label title = new Label("Appointment #" + apt.getId() + " - " + apt.getReason());
        title.getStyleClass().add("item-card-title");

        Label desc = new Label("Patient: " + apt.getPatient().getFullName() +
                "  |  Doctor: Dr. " + apt.getDoctor().getFullName() +
                " (" + apt.getDoctor().getSpecialization() + ")");
        desc.getStyleClass().add("item-card-detail");

        Label time = new Label("Date & Time: " + apt.getFormattedDateTime() +
                (apt.getNotes().isEmpty() ? "" : "  |  Notes: " + apt.getNotes()));
        time.getStyleClass().add("item-card-meta");

        details.getChildren().addAll(title, desc, time);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status badge
        Label badge = new Label(apt.getStatus().getDisplayName());
        String badgeClass = switch (apt.getStatus()) {
            case CONFIRMED -> "badge-confirmed";
            case SCHEDULED -> "badge-scheduled";
            case COMPLETED -> "badge-completed";
            case CANCELLED -> "badge-cancelled";
            default -> "badge-scheduled";
        };
        badge.getStyleClass().add(badgeClass);

        Button btnAction = new Button("Details");
        btnAction.getStyleClass().add("secondary-button");
        btnAction.setOnAction(e -> showInfoAlert("Appointment Details", apt.getDisplayInfo()));

        card.getChildren().addAll(icon, details, spacer, badge, btnAction);

        if (cardsList != null && apt.getStatus() != com.hospital.model.AppointmentStatus.CANCELLED) {
            Button btnCancel = new Button("Cancel");
            btnCancel.getStyleClass().add("danger-button");
            btnCancel.setOnAction(e -> handleCancelAppointment(apt, cardsList));
            card.getChildren().add(btnCancel);
        }

        return card;
    }

    private void handleCancelAppointment(Appointment apt, VBox cardsList) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Cancellation");
        confirm.setHeaderText("Cancel Appointment #" + apt.getId());
        confirm.setContentText("Are you sure you want to cancel the appointment for " +
                apt.getPatient().getFullName() + " with Dr. " + apt.getDoctor().getFullName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                mockDataService.getAppointmentService().cancelAppointment(apt.getId());
                refreshAppointmentCards(cardsList);
                showSuccessAlert("Appointment Cancelled",
                        "Appointment #" + apt.getId() + " has been successfully cancelled.");
            } catch (HospitalException ex) {
                showErrorAlert("Cancellation Error", ex.getMessage());
            }
        }
    }

    private HBox createReportPlaceholderCard(String title, String description, String status) {
        HBox card = new HBox(16);
        card.getStyleClass().add("item-card");
        card.setAlignment(Pos.CENTER_LEFT);

        VBox details = new VBox(4);
        Label lblT = new Label(title);
        lblT.getStyleClass().add("item-card-title");

        Label lblD = new Label(description);
        lblD.getStyleClass().add("item-card-detail");

        Label lblS = new Label(status);
        lblS.getStyleClass().add("item-card-meta");

        details.getChildren().addAll(lblT, lblD, lblS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnGen = new Button("Generate Preview");
        btnGen.getStyleClass().add("secondary-button");
        btnGen.setOnAction(e -> showInfoAlert(title,
                "Report generation pipeline configured for Phase 3 SQL queries.\nDescription: " + description));

        card.getChildren().addAll(details, spacer, btnGen);
        return card;
    }

    private ScrollPane wrapInScroll(Node content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scrollPane;
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
