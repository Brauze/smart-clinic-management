package com.project.back_end.controller;

import com.project.back_end.model.Prescription;
import com.project.back_end.service.PrescriptionService;
import com.project.back_end.service.TokenService;
import com.project.back_end.dto.PrescriptionDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {
    
    @Autowired
    private PrescriptionService prescriptionService;
    
    @Autowired
    private TokenService tokenService;
    
    @PostMapping
    public ResponseEntity<?> createPrescription(
            @Valid @RequestBody PrescriptionDTO prescriptionDTO,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            // Check if user is a doctor
            String role = tokenService.extractRole(actualToken);
            if (!"DOCTOR".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only doctors can create prescriptions"));
            }
            
            Prescription prescription = prescriptionService.createPrescription(prescriptionDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "success", true,
                        "message", "Prescription created successfully",
                        "prescription", prescription
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "error", e.getMessage()
                    ));
        }
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPrescriptionsByPatient(
            @PathVariable Long patientId,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            List<Prescription> prescriptions = prescriptionService.getPrescriptionsByPatientId(patientId);
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getPrescriptionsByDoctor(
            @PathVariable Long doctorId,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            List<Prescription> prescriptions = prescriptionService.getPrescriptionsByDoctorId(doctorId);
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getPrescriptionsByAppointment(
            @PathVariable Long appointmentId,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            List<Prescription> prescriptions = prescriptionService.getPrescriptionsByAppointmentId(appointmentId);
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getPrescriptionById(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            return prescriptionService.getPrescriptionById(id)
                    .map(prescription -> ResponseEntity.ok(prescription))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePrescription(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            // Check if user is a doctor
            String role = tokenService.extractRole(actualToken);
            if (!"DOCTOR".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Unauthorized to delete prescriptions"));
            }
            
            prescriptionService.deletePrescription(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Prescription deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", e.getMessage()
                    ));
        }
    }
}