Smart Clinic Management System - Database Schema Design
MySQL Database Design
1. Admin Table
sql
CREATE TABLE admin (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
2. Doctor Table
sql
CREATE TABLE doctor (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    qualification VARCHAR(200),
    experience_years INT DEFAULT 0,
    consultation_fee DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
3. Doctor Available Times Table
sql
CREATE TABLE doctor_available_times (
    id INT PRIMARY KEY AUTO_INCREMENT,
    doctor_id INT NOT NULL,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE,
    UNIQUE KEY unique_doctor_day_time (doctor_id, day_of_week, start_time)
);
4. Patient Table
sql
CREATE TABLE patient (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    address TEXT,
    emergency_contact VARCHAR(20),
    blood_group VARCHAR(5),
    allergies TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
5. Appointment Table
sql
CREATE TABLE appointment (
    id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_time DATETIME NOT NULL,
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW') DEFAULT 'SCHEDULED',
    reason TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE,
    INDEX idx_doctor_date (doctor_id, appointment_time),
    INDEX idx_patient_date (patient_id, appointment_time)
);
Key Design Decisions:
Primary Keys: Auto-increment integers for all tables for simplicity and performance
Foreign Keys: Proper relationships with CASCADE DELETE to maintain referential integrity
Indexes: Added on frequently queried columns (doctor_id + appointment_time, patient_id + appointment_time)
Constraints: UNIQUE constraints on emails, ENUM for controlled values
Timestamps: Automatic creation and update timestamps for audit trail
MongoDB Collection Design
Prescription Collection
json
{
  "_id": ObjectId("64a1b2c3d4e5f6789012345"),
  "appointmentId": 123,
  "patientId": 456,
  "doctorId": 789,
  "patientName": "John Doe",
  "doctorName": "Dr. Sarah Wilson",
  "prescriptionDate": "2024-01-15T10:30:00Z",
  "medications": [
    {
      "name": "Amoxicillin",
      "dosage": "500mg",
      "frequency": "Three times daily",
      "duration": "7 days",
      "instructions": "Take with food"
    },
    {
      "name": "Ibuprofen",
      "dosage": "200mg",
      "frequency": "As needed",
      "duration": "5 days",
      "instructions": "Take for pain relief, max 3 times daily"
    }
  ],
  "diagnosis": "Upper respiratory infection",
  "symptoms": ["Cough", "Fever", "Sore throat"],
  "vitalSigns": {
    "temperature": "101.2°F",
    "bloodPressure": "120/80",
    "heartRate": 85,
    "weight": "70kg"
  },
  "followUpRequired": true,
  "followUpDate": "2024-01-22T10:30:00Z",
  "notes": "Patient responded well to treatment. Continue medication as prescribed.",
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:45:00Z",
  "updatedAt": "2024-01-15T10:45:00Z"
}
System Logs Collection
json
{
  "_id": ObjectId("64a1b2c3d4e5f6789012346"),
  "timestamp": "2024-01-15T14:30:00Z",
  "level": "INFO",
  "action": "APPOINTMENT_BOOKED",
  "userId": 456,
  "userType": "PATIENT",
  "details": {
    "appointmentId": 123,
    "doctorId": 789,
    "appointmentTime": "2024-01-20T09:00:00Z"
  },
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
  "sessionId": "sess_abc123def456"
}
MongoDB Design Justifications:
Flexible Schema: Prescriptions can have varying numbers of medications and different fields
Nested Documents: Medications array allows multiple prescriptions in one document
Embedded Data: Vital signs and symptoms are embedded for faster retrieval
Indexing Strategy: Would index on patientId, doctorId, appointmentId, and prescriptionDate for fast queries
Document Size: Designed to stay well under MongoDB's 16MB document limit
Audit Trail: Comprehensive logging with timestamps and user context for security and compliance
