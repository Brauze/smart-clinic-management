package com.project.back_end.service;

import com.project.back_end.dto.AppointmentDTO;
import com.project.back_end.dto.AppointmentBookingDTO;
import com.project.back_end.dto.ApiResponseDTO;
import com.project.back_end.model.Appointment;
import com.project.back_end.model.Doctor;
import com.project.back_end.model.Patient;
import com.project.back_end.repository.AppointmentRepository;
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    /**
     * Book a new appointment
     */
    public ApiResponseDTO<AppointmentDTO> bookAppointment(AppointmentBookingDTO bookingDTO) {
        try {
            // Validate doctor exists
            Optional<Doctor> doctorOpt = doctorRepository.findById(bookingDTO.getDoctorId());
            if (doctorOpt.isEmpty()) {
                return new ApiResponseDTO<>(false, "Doctor not found", null);
            }
            
            // Validate patient exists
            Optional<Patient> patientOpt = patientRepository.findById(bookingDTO.getPatientId());
            if (patientOpt.isEmpty()) {
                return new ApiResponseDTO<>(false, "Patient not found", null);
            }
            
            Doctor doctor = doctorOpt.get();
            Patient patient = patientOpt.get();
            
            // Check if the appointment time is available
            if (!isAppointmentTimeAvailable(doctor.getId(), bookingDTO.getAppointmentTime())) {
                return new ApiResponseDTO<>(false, "The selected time slot is not available", null);
            }
            
            // Create and save appointment
            Appointment appointment = new Appointment();
            appointment.setDoctor(doctor);
            appointment.setPatient(patient);
            appointment.setAppointmentTime(bookingDTO.getAppointmentTime());
            appointment.setReason(bookingDTO.getReason());
            appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
            
            Appointment savedAppointment = appointmentRepository.save(appointment);
            
            // Convert to DTO
            AppointmentDTO appointmentDTO = convertToDTO(savedAppointment);
            
            return new ApiResponseDTO<>(true, "Appointment booked successfully", appointmentDTO);
            
        } catch (Exception e) {
            return new ApiResponseDTO<>(false, "Error booking appointment: " + e.getMessage(), null);
        }
    }
    
    /**
     * Get appointments for a doctor on a specific date
     */
    public List<AppointmentDTO> getAppointmentsForDoctorOnDate(Long doctorId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        List<Appointment> appointments = appointmentRepository
            .findByDoctorIdAndAppointmentTimeBetweenOrderByAppointmentTime(
                doctorId, startOfDay, endOfDay);
        
        return appointments.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all appointments for a doctor
     */
    public List<AppointmentDTO> getAppointmentsForDoctor(Long doctorId) {
        List<Appointment> appointments = appointmentRepository
            .findByDoctorIdOrderByAppointmentTimeDesc(doctorId);
        
        return appointments.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all appointments for a patient
     */
    public List<AppointmentDTO> getAppointmentsForPatient(Long patientId) {
        List<Appointment> appointments = appointmentRepository
            .findByPatientIdOrderByAppointmentTimeDesc(patientId);
        
        return appointments.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get upcoming appointments for a patient
     */
    public List<AppointmentDTO> getUpcomingAppointmentsForPatient(Long patientId) {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> appointments = appointmentRepository
            .findByPatientIdAndAppointmentTimeAfterAndStatusOrderByAppointmentTime(
                patientId, now, Appointment.AppointmentStatus.SCHEDULED);
        
        return appointments.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get upcoming appointments for a doctor
     */
    public List<AppointmentDTO> getUpcomingAppointmentsForDoctor(Long doctorId) {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> appointments = appointmentRepository
            .findByDoctorIdAndAppointmentTimeAfterAndStatusOrderByAppointmentTime(
                doctorId, now, Appointment.AppointmentStatus.SCHEDULED);
        
        return appointments.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Cancel an appointment
     */
    public ApiResponseDTO<AppointmentDTO> cancelAppointment(Long appointmentId, String userEmail, String userRole) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (appointmentOpt.isEmpty()) {
                return new ApiResponseDTO<>(false, "Appointment not found", null);
            }
            
            Appointment appointment = appointmentOpt.get();
            
            // Check if user has permission to cancel
            if (!"ADMIN".equals(userRole) && 
                !appointment.getPatient().getEmail().equals(userEmail) &&
                !appointment.getDoctor().getEmail().equals(userEmail)) {
                return new ApiResponseDTO<>(false, "You don't have permission to cancel this appointment", null);
            }
            
            // Check if appointment can be cancelled (not in the past and not already cancelled/completed)
            if (!appointment.canBeCancelled()) {
                return new ApiResponseDTO<>(false, "This appointment cannot be cancelled", null);
            }
            
            appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
            Appointment savedAppointment = appointmentRepository.save(appointment);
            
            AppointmentDTO appointmentDTO = convertToDTO(savedAppointment);
            return new ApiResponseDTO<>(true, "Appointment cancelled successfully", appointmentDTO);
            
        } catch (Exception e) {
            return new ApiResponseDTO<>(false, "Error cancelling appointment: " + e.getMessage(), null);
        }
    }
    
    /**
     * Complete an appointment (Doctor only)
     */
    public ApiResponseDTO<AppointmentDTO> completeAppointment(Long appointmentId, String notes) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (appointmentOpt.isEmpty()) {
                return new ApiResponseDTO<>(false, "Appointment not found", null);
            }
            
            Appointment appointment = appointmentOpt.get();
            appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
            appointment.setNotes(notes);
            
            Appointment savedAppointment = appointmentRepository.save(appointment);
            AppointmentDTO appointmentDTO = convertToDTO(savedAppointment);
            
            return new ApiResponseDTO<>(true, "Appointment marked as completed", appointmentDTO);
            
        } catch (Exception e) {
            return new ApiResponseDTO<>(false, "Error completing appointment: " + e.getMessage(), null);
        }
    }
    
    /**
     * Check if appointment time is available for a doctor
     */
    private boolean isAppointmentTimeAvailable(Long doctorId, LocalDateTime appointmentTime) {
        // Check if there's already an appointment at this time (30-minute slots)
        LocalDateTime startTime = appointmentTime.minusMinutes(29);
        LocalDateTime endTime = appointmentTime.plusMinutes(29);
        
        List<Appointment> conflictingAppointments = appointmentRepository
            .findByDoctorIdAndAppointmentTimeBetweenAndStatus(
                doctorId, startTime, endTime, Appointment.AppointmentStatus.SCHEDULED);
        
        return conflictingAppointments.isEmpty();
    }
    
    /**
     * Get appointment by ID
     */
    public AppointmentDTO getAppointmentById(Long appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        return appointmentOpt.map(this::convertToDTO).orElse(null);
    }
    
    /**
     * Convert Appointment entity to DTO
     */
    private AppointmentDTO convertToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus().toString());
        dto.setReason(appointment.getReason());
        dto.setNotes(appointment.getNotes());
        
        // Set patient info
        if (appointment.getPatient() != null) {
            dto.setPatientId(appointment.getPatient().getId());
            dto.setPatientName(appointment.getPatient().getName());
            dto.setPatientEmail(appointment.getPatient().getEmail());
        }
        
        // Set doctor info
        if (appointment.getDoctor() != null) {
            dto.setDoctorId(appointment.getDoctor().getId());
            dto.setDoctorName(appointment.getDoctor().getName());
            dto.setDoctorSpecialty(appointment.getDoctor().getSpecialty());
        }
        
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        
        return dto;
    }
}
