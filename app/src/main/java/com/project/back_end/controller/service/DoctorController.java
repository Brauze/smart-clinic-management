package com.project.back_end.controller;

import com.project.back_end.dto.DoctorDTO;
import com.project.back_end.dto.DoctorAvailabilityDTO;
import com.project.back_end.dto.LoginRequestDTO;
import com.project.back_end.dto.ApiResponseDTO;
import com.project.back_end.model.Doctor;
import com.project.back_end.service.DoctorService;
import com.project.back_end.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private TokenService tokenService;
    
    /**
     * Get all doctors
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<DoctorDTO>>> getAllDoctors(
            @RequestHeader("Authorization") String token) {
        try {
            // Validate token
            if (!tokenService.validateToken(token.replace("Bearer ", ""))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(false, "Invalid or expired token", null));
            }
            
            List<DoctorDTO> doctors = doctorService.getAllDoctors();
            return ResponseEntity.ok(
                new ApiResponseDTO<>(true, "Doctors retrieved successfully", doctors)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Error retrieving doctors: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get doctor availability for a specific date and specialty
     */
    @GetMapping("/availability")
    public ResponseEntity<ApiResponseDTO<List<DoctorAvailabilityDTO>>> getDoctorAvailability(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String timeSlot) {
        try {
            // Validate token
            String jwtToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(false, "Invalid or expired token", null));
            }
            
            // Get available doctors for the specified criteria
            List<DoctorAvailabilityDTO> availability = doctorService.getDoctorAvailability(date, specialty, timeSlot);
            
            if (availability.isEmpty()) {
                return ResponseEntity.ok(
                    new ApiResponseDTO<>(true, "No doctors available for the specified criteria", availability)
                );
            }
            
            return ResponseEntity.ok(
                new ApiResponseDTO<>(true, "Doctor availability retrieved successfully", availability)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Error retrieving doctor availability: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get doctor by specialty
     */
    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<ApiResponseDTO<List<DoctorDTO>>> getDoctorsBySpecialty(
            @RequestHeader("Authorization") String token,
            @PathVariable String specialty) {
        try {
            // Validate token
            if (!tokenService.validateToken(token.replace("Bearer ", ""))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(false, "Invalid or expired token", null));
            }
            
            List<DoctorDTO> doctors = doctorService.getDoctorsBySpecialty(specialty);
            return ResponseEntity.ok(
                new ApiResponseDTO<>(true, "Doctors retrieved successfully", doctors)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Error retrieving doctors by specialty: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get doctor by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<DoctorDTO>> getDoctorById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            // Validate token
            if (!tokenService.validateToken(token.replace("Bearer ", ""))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(false, "Invalid or expired token", null));
            }
            
            DoctorDTO doctor = doctorService.getDoctorById(id);
            if (doctor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(false, "Doctor not found", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponseDTO<>(true, "Doctor retrieved successfully", doctor)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Error retrieving doctor: " + e.getMessage(), null));
        }
    }
    
    /**
     * Doctor login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<String>> doctorLogin(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            ApiResponseDTO<String> response = doctorService.authenticateDoctor(loginRequest.getEmail(), loginRequest.getPassword());
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Login error: " + e.getMessage(), null));
        }
    }
    
    /**
     * Add new doctor (Admin only)
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<DoctorDTO>> addDoctor(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody DoctorDTO doctorDTO) {
        try {
            // Validate token and check if user is admin
            String jwtToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(jwtToken) || !tokenService.isAdmin(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponseDTO<>(false, "Admin access required", null));
            }
            
            DoctorDTO savedDoctor = doctorService.saveDoctor(doctorDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Doctor created successfully", savedDoctor));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Error creating doctor: " + e.getMessage(), null));
        }
    }
    
    /**
     * Update doctor information
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<DoctorDTO>> updateDoctor(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @RequestBody DoctorDTO doctorDTO) {
        try {
            // Validate token
            String jwtToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>(false, "Invalid or expired token", null));
            }
            
            // Check if the user is admin or the doctor themselves
            String userEmail = tokenService.getEmailFromToken(jwtToken);
            if (!tokenService.isAdmin(jwtToken) && !doctorService.isDoctorEmail(userEmail, id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponseDTO<>(false, "Access denied", null));
            }
            
            DoctorDTO updatedDoctor = doctorService.updateDoctor(id, doctorDTO);
            if (updatedDoctor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(false, "Doctor not found", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponseDTO<>(true, "Doctor updated successfully", updatedDoctor)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Error updating doctor: " + e.getMessage(), null));
        }
    }
    
    /**
     * Delete doctor (Admin only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteDoctor(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            // Validate token and check if user is admin
            String jwtToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(jwtToken) || !tokenService.isAdmin(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponseDTO<>(false, "Admin access required", null));
            }
            
            boolean deleted = doctorService.deleteDoctor(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(false, "Doctor not found", null));
            }
            
            return ResponseEntity.ok(
                new ApiResponseDTO<>(true, "Doctor deleted successfully", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "Error deleting doctor: " + e.getMessage(), null));
        }
    }
}
