package com.project.back_end.service;

import com.project.back_end.model.Doctor;
import com.project.back_end.model.Appointment;
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.repository.AppointmentRepository;
import com.project.back_end.dto.LoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private TokenService tokenService;
    
    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }
    
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
    
    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }
    
    public List<Doctor> getDoctorsBySpecialty(String specialty) {
        return doctorRepository.findBySpecialty(specialty);
    }
    
    public List<Doctor> searchDoctorsByName(String name) {
        return doctorRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<LocalDateTime> getAvailableTimesForDate(Long doctorId, LocalDate date) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        Doctor doctor = doctorOpt.get();
        List<LocalDateTime> availableTimes = doctor.getAvailableTimes();
        
        // Filter times for the specified date
        List<LocalDateTime> timesForDate = availableTimes.stream()
            .filter(time -> time.toLocalDate().equals(date))
            .collect(Collectors.toList());
        
        // Remove times that already have appointments
        List<Appointment> appointments = appointmentRepository
            .findByDoctorAndAppointmentTimeBetween(doctor, 
                date.atStartOfDay(), 
                date.atTime(LocalTime.MAX));
        
        Set<LocalDateTime> bookedTimes = appointments.stream()
            .map(Appointment::getAppointmentTime)
            .collect(Collectors.toSet());
        
        return timesForDate.stream()
            .filter(time -> !bookedTimes.contains(time))
            .sorted()
            .collect(Collectors.toList());
    }
    
    public Map<String, Object> validateDoctorLogin(LoginDTO loginDTO) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Doctor> doctorOpt = doctorRepository.findByEmail(loginDTO.getEmail());
        if (doctorOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Invalid email or password");
            return response;
        }
        
        Doctor doctor = doctorOpt.get();
        if (!doctor.getPassword().equals(loginDTO.getPassword())) {
            response.put("success", false);
            response.put("message", "Invalid email or password");
            return response;
        }
        
        String token = tokenService.generateTokenWithRole(doctor.getEmail(), "DOCTOR");
        response.put("success", true);
        response.put("token", token);
        response.put("doctor", doctor);
        response.put("message", "Login successful");
        
        return response;
    }
    
    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }
    
    public Doctor updateDoctor(Long id, Doctor doctorDetails) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(id);
        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            doctor.setName(doctorDetails.getName());
            doctor.setSpecialty(doctorDetails.getSpecialty());
            doctor.setPhone(doctorDetails.getPhone());
            doctor.setAvailableTimes(doctorDetails.getAvailableTimes());
            return doctorRepository.save(doctor);
        }
        return null;
    }
    
    public List<Doctor> getDoctorsBySpecialtyAndTime(String specialty, LocalDateTime time) {
        return doctorRepository.findBySpecialtyAndAvailableTime(specialty, time);
    }
}