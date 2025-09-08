package com.project.back_end.controller;

import com.project.back_end.service.AdminService;
import com.project.back_end.service.DoctorService;
import com.project.back_end.service.PatientService;
import com.project.back_end.dto.LoginDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private PatientService patientService;
    
    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            Map<String, Object> response = adminService.validateAdminLogin(loginDTO);
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
    
    @PostMapping("/login/doctor")
    public ResponseEntity<?> loginDoctor(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            Map<String, Object> response = doctorService.validateDoctorLogin(loginDTO);
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
    
    @PostMapping("/login/patient")
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
}