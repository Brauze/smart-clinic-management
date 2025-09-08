package com.project.back_end.controller;

import com.project.back_end.model.Patient;
import com.project.back_end.model.Appointment;
import com.project.back_end.service.PatientService;
import com.project.back_end.service.AppointmentService;
import com.project.back_end.service.TokenService;
import com.project.back_end.dto.LoginDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*")
public class PatientController {
    
    @Autowired
    private PatientService patientService;
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private TokenService tokenService;
    
    @PostMapping("/register")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody Patient patient) {
        try {
            Patient createdPatient = patientService.createPatient(patient);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPatient);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> loginPatient(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            Map<String, Object> response = patientService.validatePatientLogin(loginDTO);
            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/appointments")
    public ResponseEntity<?> getPatientAppointments(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            List<Appointment> appointments = appointmentService.getAppointmentsByPatient(id);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllPatients(@RequestHeader("Authorization") String token) {
        try {
            // Validate token and check admin role
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            String role = tokenService.extractRole(actualToken);
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only admins can view all patients"));
            }
            
            List<Patient> patients = patientService.getAllPatients();
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            return patientService.getPatientById(id)
                    .map(patient -> ResponseEntity.ok(patient))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}