package com.project.back_end.controller;

import com.project.back_end.model.Doctor;
import com.project.back_end.service.DoctorService;
import com.project.back_end.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private TokenService tokenService;
    
    @GetMapping
    public ResponseEntity<?> getAllDoctors() {
        try {
            List<Doctor> doctors = doctorService.getAllDoctors();
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id)
                .map(doctor -> ResponseEntity.ok(doctor))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/availability/{doctorId}")
    public ResponseEntity<?> getDoctorAvailability(
            @PathVariable Long doctorId,
            @RequestParam LocalDate date,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            List<LocalDateTime> availableTimes = doctorService.getAvailableTimesForDate(doctorId, date);
            
            Map<String, Object> response = new HashMap<>();
            response.put("doctorId", doctorId);
            response.put("date", date);
            response.put("availableTimes", availableTimes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<?> getDoctorsBySpecialty(
            @PathVariable String specialty,
            @RequestParam(required = false) LocalDateTime time) {
        
        try {
            List<Doctor> doctors;
            if (time != null) {
                doctors = doctorService.getDoctorsBySpecialtyAndTime(specialty, time);
            } else {
                doctors = doctorService.getDoctorsBySpecialty(specialty);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("specialty", specialty);
            response.put("doctors", doctors);
            if (time != null) {
                response.put("time", time);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createDoctor(
            @Valid @RequestBody Doctor doctor,
            @RequestHeader("Authorization") String token) {
        
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
                        .body(Map.of("error", "Only admins can create doctors"));
            }
            
            Doctor createdDoctor = doctorService.createDoctor(doctor);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDoctor);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody Doctor doctor,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            Doctor updatedDoctor = doctorService.updateDoctor(id, doctor);
            if (updatedDoctor != null) {
                return ResponseEntity.ok(updatedDoctor);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDoctor(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        
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
                        .body(Map.of("error", "Only admins can delete doctors"));
            }
            
            doctorService.deleteDoctor(id);
            return ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchDoctors(@RequestParam String name) {
        try {
            List<Doctor> doctors = doctorService.searchDoctorsByName(name);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}