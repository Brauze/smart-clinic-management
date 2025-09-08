package com.project.back_end.service;

import com.project.back_end.model.Patient;
import com.project.back_end.repository.PatientRepository;
import com.project.back_end.dto.LoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PatientService {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private TokenService tokenService;
    
    public Patient createPatient(Patient patient) {
        if (patientRepository.existsByEmail(patient.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        return patientRepository.save(patient);
    }
    
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
    
    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }
    
    public Optional<Patient> getPatientByEmail(String email) {
        return patientRepository.findByEmail(email);
    }
    
    public List<Patient> searchPatientsByName(String name) {
        return patientRepository.findByNameContainingIgnoreCase(name);
    }
    
    public Map<String, Object> validatePatientLogin(LoginDTO loginDTO) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Patient> patientOpt = patientRepository.findByEmail(loginDTO.getEmail());
        if (patientOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Invalid email or password");
            return response;
        }
        
        Patient patient = patientOpt.get();
        if (!patient.getPassword().equals(loginDTO.getPassword())) {
            response.put("success", false);
            response.put("message", "Invalid email or password");
            return response;
        }
        
        String token = tokenService.generateTokenWithRole(patient.getEmail(), "PATIENT");
        response.put("success", true);
        response.put("token", token);
        response.put("patient", patient);
        response.put("message", "Login successful");
        
        return response;
    }
    
    public Patient updatePatient(Long id, Patient patientDetails) {
        Optional<Patient> patientOpt = patientRepository.findById(id);
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            patient.setName(patientDetails.getName());
            patient.setPhone(patientDetails.getPhone());
            patient.setAddress(patientDetails.getAddress());
            patient.setDateOfBirth(patientDetails.getDateOfBirth());
            return patientRepository.save(patient);
        }
        return null;
    }
    
    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }
}