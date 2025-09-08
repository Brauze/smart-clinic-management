package com.project.back_end.controller;

import com.project.back_end.model.Appointment;
import com.project.back_end.service.AppointmentService;
import com.project.back_end.service.TokenService;
import com.project.back_end.dto.AppointmentDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private TokenService tokenService;
    
    @PostMapping
    public ResponseEntity<?> bookAppointment(
            @Valid @RequestBody AppointmentDTO appointmentDTO,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            Appointment appointment = appointmentService.bookAppointment(appointmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getAppointmentsByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false) LocalDateTime date,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            List<Appointment> appointments;
            if (date != null) {
                appointments = appointmentService.getAppointmentsByDoctorAndDate(doctorId, date);
            } else {
                appointments = appointmentService.getAppointmentsByDoctor(doctorId);
            }
            
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getAppointmentsByPatient(
            @PathVariable Long patientId,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patientId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam Appointment.AppointmentStatus status,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            Appointment updatedAppointment = appointmentService.updateAppointmentStatus(id, status);
            if (updatedAppointment != null) {
                return ResponseEntity.ok(updatedAppointment);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        
        try {
            // Validate token
            String actualToken = token.replace("Bearer ", "");
            if (!tokenService.validateToken(actualToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            
            appointmentService.cancelAppointment(id);
            return ResponseEntity.ok(Map.of("message", "Appointment cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}