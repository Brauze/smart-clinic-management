package com.project.back_end.repository;

import com.project.back_end.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    /**
     * Find patient by email address
     */
    Optional<Patient> findByEmail(String email);
    
    /**
     * Find patient by email (case insensitive)
     */
    @Query("SELECT p FROM Patient p WHERE LOWER(p.email) = LOWER(:email)")
    Optional<Patient> findByEmailIgnoreCase(@Param("email") String email);
    
    /**
     * Check if patient exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Find patients by name (partial match, case insensitive)
     */
    @Query("SELECT p FROM Patient p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Patient> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Find patients by phone number
     */
    Optional<Patient> findByPhone(String phone);
    
    /**
     * Find patients by date of birth
     */
    List<Patient> findByDateOfBirth(LocalDate dateOfBirth);
    
    /**
     * Find patients by blood group
     */
    List<Patient> findByBloodGroup(String bloodGroup);
    
    /**
     * Find patients by gender
     */
    List<Patient> findByGender(Patient.Gender gender);
    
    /**
     * Find patients registered after a specific date
     */
    @Query("SELECT p FROM Patient p WHERE p.createdAt >= :date")
    List<Patient> findPatientsRegisteredAfter(@Param("date") LocalDate date);
    
    /**
     * Find patients with allergies containing specific text
     */
    @Query("SELECT p FROM Patient p WHERE p.allergies IS NOT NULL AND LOWER(p.allergies) LIKE LOWER(CONCAT('%', :allergy, '%'))")
    List<Patient> findByAllergiesContaining(@Param("allergy") String allergy);
    
    /**
     * Count total number of patients
     */
    @Query("SELECT COUNT(p) FROM Patient p")
    Long countTotalPatients();
    
    /**
     * Find patients by emergency contact
     */
    List<Patient> findByEmergencyContact(String emergencyContact);
    
    /**
     * Find patients with upcoming appointments (custom query)
     */
    @Query("SELECT DISTINCT p FROM Patient p JOIN p.appointments a WHERE a.appointmentTime > CURRENT_TIMESTAMP AND a.status = 'SCHEDULED'")
    List<Patient> findPatientsWithUpcomingAppointments();
    
    /**
     * Search patients by multiple criteria
     */
    @Query("SELECT p FROM Patient p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(p.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:phone IS NULL OR p.phone LIKE CONCAT('%', :phone, '%'))")
    List<Patient> searchPatients(@Param("name") String name, 
                               @Param("email") String email, 
                               @Param("phone") String phone);
    
    /**
     * Find patients by age range (calculated from date of birth)
     */
    @Query("SELECT p FROM Patient p WHERE " +
           "YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) - " +
           "CASE WHEN MONTH(CURRENT_DATE) < MONTH(p.dateOfBirth) OR " +
           "(MONTH(CURRENT_DATE) = MONTH(p.dateOfBirth) AND DAY(CURRENT_DATE) < DAY(p.dateOfBirth)) " +
           "THEN 1 ELSE 0 END BETWEEN :minAge AND :maxAge")
    List<Patient> findPatientsByAgeRange(@Param("minAge") int minAge, @Param("maxAge") int maxAge);
    
    /**
     * Find patients who haven't had appointments in the last N months
     */
    @Query("SELECT p FROM Patient p WHERE p.id NOT IN " +
           "(SELECT DISTINCT a.patient.id FROM Appointment a " +
           "WHERE a.appointmentTime >= :sinceDate)")
    List<Patient> findPatientsWithoutRecentAppointments(@Param("sinceDate") LocalDate sinceDate);
}
