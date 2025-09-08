package com.project.back_end.service;

import com.project.back_end.model.Appointment;
import com.project.back_end.model.Doctor;
import com.project.back_end.model.Patient;
import com.project.back_end.repository.AppointmentRepository;
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.repository.PatientRepository;
import com.project.back_end.dto.AppointmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    public Appointment bookAppointment(AppointmentDTO appointmentDTO) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(appointmentDTO.getDoctorId());
        Optional<Patient> patientOpt = patientRepository.findById(appointmentDTO.getPatientId());
        
        if (doctorOpt.isEmpty() || patientOpt.isEmpty()) {
            throw new RuntimeException("Doctor or Patient not found");
        }
        
        Doctor doctor = doctorOpt.get();
        Patient patient = patientOpt.get();
        
        // Check if the time slot is already booked
        if (appointmentRepository.existsByDoctorAndAppointmentTime(doctor, appointmentDTO.getAppointmentTime())) {
            throw new RuntimeException("This time slot is already booked");
        }
        
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentTime(appointmentDTO.getAppointmentTime());
        appointment.setReason(appointmentDTO.getReason());
        appointment.setDurationMinutes(appointmentDTO.getDurationMinutes() != null ? 
                                       appointmentDTO.getDurationMinutes() : 30);
        
        return appointmentRepository.save(appointment);
    }
    
    public List<Appointment> getAppointmentsByDoctorAndDate(Long doctorId, LocalDateTime date) {
        return appointmentRepository.findByDoctorAndDate(doctorId, date);
    }
    
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            return List.of();
        }
        return appointmentRepository.findByPatient(patientOpt.get());
    }
    
    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return List.of();
        }
        return appointmentRepository.findByDoctor(doctorOpt.get());
    }
    
    public Appointment updateAppointmentStatus(Long appointmentId, Appointment.AppointmentStatus status) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setStatus(status);
            return appointmentRepository.save(appointment);
        }
        return null;
    }
    
    public void cancelAppointment(Long appointmentId) {
        updateAppointmentStatus(appointmentId, Appointment.AppointmentStatus.CANCELLED);
    }
    
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
}