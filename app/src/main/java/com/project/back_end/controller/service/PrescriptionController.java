package com.project.back_end.controller;

import com.project.back_end.dto.PrescriptionDTO;
import com.project.back_end.dto.ApiResponseDTO;
import com.project.back_end.model.Prescription;
import com.project.back_end.service.PrescriptionService;
import com.project.back_end.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {
    
    @Autowired
    private PrescriptionService prescriptionService;
    
    @Autowired
    private TokenService tokenService;
    
    /**
     * Create a new prescription (Doctor only)
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<PrescriptionDTO>> createPrescription(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PrescriptionDTO prescriptionDTO) {
        try {
            // Validate token
            String jwtToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(false, "Invalid or expired token", null));
            }
            
            // Check if user is a doctor
            String userRole = tokenService.getRoleFromToken(jwtToken);
            if (!"DOCTOR".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponseDTO<>(false, "Only doctors can create prescriptions", null));
            }
            
            // Get doctor email from token
            String doctorEmail = tokenService.getEmailFromToken(jwtToken);
            prescriptionDTO.setDoctorEmail(doctorEmail);
            
            // Create prescription
            ApiResponseDTO<PrescriptionDTO> response = prescriptionService.createPrescription(prescriptionDTO);
            
            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Error creating prescription: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get all prescriptions for a patient
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponseDTO<List<PrescriptionDTO>>> getPrescriptionsForPatient(
            @RequestHeader("Authorization") String token,
            @PathVariable Long patientId) {
        try {
            // Validate token
            String jwtToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(false, "Invalid or expired token", null));
            }
            
            // Check authorization - patient can view their own prescriptions, doctors and admins can view any
            String userRole = tokenService.getRoleFromToken(jwtToken);
            String userEmail = tokenService.getEmailFromToken(jwtToken);
            
            if ("PATIENT".equals(userRole)) {
                // Check if the patient is requesting their own prescriptions
                if (!prescriptionService.isPatientOwner(patientId, userEmail)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponseDTO<>(false, "Access denied", null));
                }
            }
            
            List<PrescriptionDTO> prescriptions = prescriptionService.getPrescriptionsForPatient(patientId);
            return ResponseEntity.ok(
                new ApiResponseDTO<>(true, "Prescriptions retrieved successfully", prescriptions)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Error retrieving prescriptions: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get all prescriptions by a doctor
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponseDTO<List<PrescriptionDTO>>> getPrescriptionsByDoctor(
            @RequestHeader("Authorization") String token,
            @PathVariable Long doctorId) {
        try {
            // Validate token
            String jwtToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(false, "Invalid or expired token", null));
            }
            
            // Check authorization - doctors can view their own prescriptions, admins can view any
            String userRole = tokenService.getRoleFromToken(jwtToken);
            String userEmail = tokenService.getEmailFromToken(jwtToken);
            
            if ("DOCTOR".equals(userRole)) {
                if (!prescriptionService.isDoctorOwner(doctorId, userEmail)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponseDTO<>(false, "Access denied", null));
                }
            } else if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponseDTO<>(false, "Access denied", null));
            }
            
            List<PrescriptionDTO> prescriptions = prescriptionService.getPrescriptionsByDoctor(doctorId);
            return ResponseEntity.ok(
                new ApiResponseDTO<>(true, "Prescriptions retrieved successfully", prescriptions)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Error retrieving prescriptions: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get prescription by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<PrescriptionDTO>> getPrescriptionById(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {
        try {
            // Validate token
            String jwtToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(false, "Invalid or expired token", null));
            }
            
            PrescriptionDTO prescription = prescriptionService.getPrescriptionById(id);
            if (prescription == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(false, "Prescription not found", null));
            }
            
            // Check authorization
            String userRole = tokenService.getRoleFromToken(jwtToken);
            String userEmail = tokenService.getEmailFromToken(jwtToken);
            
            if ("PATIENT".equals(userRole) && !prescription.getPatientEmail().equals(userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponseDTO<>(false, "Access denied", null));
            } else if ("DOCTOR".equals(userRole) && !prescription.getDoctorEmail().equals(userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponseDTO<>(false, "Access denied", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponseDTO<>(true, "Prescription retrieved successfully", prescription)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Error retrieving prescription: " + e.getMessage(), null));
        }
    }
    
    /**
     * Update prescription status (Doctor only)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponseDTO<PrescriptionDTO>> updatePrescriptionStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @RequestParam String status) {
        try {
            // Validate token
            String jwtToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(false, "Invalid or expired token", null));
            }
            
            // Check if user is a doctor
            String userRole = tokenService.getRoleFromToken(jwtToken);
            if (!"DOCTOR".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponseDTO<>(false, "Only doctors can update prescription status", null));
            }
            
            String doctorEmail = tokenService.getEmailFromToken(jwtToken);
            ApiResponseDTO<PrescriptionDTO> response = prescriptionService.updatePrescriptionStatus(id, status, doctorEmail);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Error updating prescription: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get prescriptions for an appointment
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponseDTO<List<PrescriptionDTO>>> getPrescriptionsForAppointment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long appointmentId) {
        try {
            // Validate token
            String jwtToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(false, "Invalid or expired token", null));
            }
            
            List<PrescriptionDTO> prescriptions = prescriptionService.getPrescriptionsForAppointment(appointmentId);
            return ResponseEntity.ok(
                new ApiResponseDTO<>(true, "Prescriptions retrieved successfully", prescriptions)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Error retrieving prescriptions: " + e.getMessage(), null));
        }
    }
    
    /**
     * Delete prescription (Doctor who created it or Admin only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deletePrescription(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {
        try {
            // Validate token
            String jwtToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(false, "Invalid or expired token", null));
            }
            
            String userRole = tokenService.getRoleFromToken(jwtToken);
            String userEmail = tokenService.getEmailFromToken(jwtToken);
            
            ApiResponseDTO<Void> response = prescriptionService.deletePrescription(id, userEmail, userRole);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Error deleting prescription: " + e.getMessage(), null));
        }
    }
}
