package com.project.back_end.service;

import com.project.back_end.dto.DoctorDTO;
import com.project.back_end.dto.DoctorAvailabilityDTO;
import com.project.back_end.dto.ApiResponseDTO;
import com.project.back_end.model.Doctor;
import com.project.back_end.model.DoctorAvailableTime;
import com.project.back_end.model.Appointment;
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.repository.DoctorAvailableTimeRepository;
import com.project.back_end.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@Transactional
public class DoctorService {
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private DoctorAvailableTimeRepository availableTimeRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Get available time slots for a doctor on a given date
     */
    public List<String> getAvailableTimeSlotsForDoctor(Long doctorId, LocalDate date) {
        List<String> availableSlots = new ArrayList<>();
        
        try {
            // Get doctor's available times for the day of week
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            List<DoctorAvailableTime> doctorTimes = availableTimeRepository
                .findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, dayOfWeek);
            
            if (doctorTimes.isEmpty()) {
                return availableSlots; // No availability for this day
            }
            
            // Get existing appointments for the date
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            List<Appointment> existingAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetweenAndStatus(
                    doctorId, startOfDay, endOfDay, Appointment.AppointmentStatus.SCHEDULED);
            
            // Generate available slots based on doctor's schedule
            for (DoctorAvailableTime availableTime : doctorTimes) {
                LocalTime startTime = availableTime.getStartTime();
                LocalTime endTime = availableTime.getEndTime();
                
                // Generate 30-minute slots
                LocalTime currentSlot = startTime;
                while (currentSlot.isBefore(endTime)) {
                    LocalDateTime slotDateTime = date.atTime(currentSlot);
                    
                    // Check if slot is not already booked
                    boolean isBooked = existingAppointments.stream()
                        .anyMatch(apt -> apt.getAppointmentTime().equals(slotDateTime));
                    
                    if (!isBooked && slotDateTime.isAfter(LocalDateTime.now())) {
                        availableSlots.add(currentSlot.toString());
                    }
                    
                    currentSlot = currentSlot.plusMinutes(30);
                }
            }
        } catch (Exception e) {
            // Log error and return empty list
            System.err.println("Error getting available time slots: " + e.getMessage());
        }
        
        return availableSlots;
    }
    
    /**
     * Validate doctor login credentials and return structured response
     */
    public ApiResponseDTO<String> authenticateDoctor(String email, String password) {
        try {
            Optional<Doctor> doctorOpt = doctorRepository.findByEmail(email);
            
            if (doctorOpt.isEmpty()) {
                return new ApiResponseDTO<>(false, "Invalid email or password", null);
            }
            
            Doctor doctor = doctorOpt.get();
            
            // Verify password
            if (!passwordEncoder.matches(password, doctor.getPassword())) {
                return new ApiResponseDTO<>(false, "Invalid email or password", null);
            }
            
            // Generate JWT token
            String token = tokenService.generateToken(doctor.getEmail(), "DOCTOR", doctor.getId());
            
            return new ApiResponseDTO<>(true, "Login successful", token);
            
        } catch (Exception e) {
            return new ApiResponseDTO<>(false, "Login error: " + e.getMessage(), null);
        }
    }
    
    /**
     * Get doctor availability with filter options
     */
    public List<DoctorAvailabilityDTO> getDoctorAvailability(LocalDate date, String specialty, String timeSlot) {
        List<DoctorAvailabilityDTO> availabilityList = new ArrayList<>();
        
        try {
            // Get doctors based on specialty filter
            List<Doctor> doctors;
            if (specialty != null && !specialty.trim().isEmpty()) {
                doctors = doctorRepository.findBySpecialtyContainingIgnoreCase(specialty);
            } else {
                doctors = doctorRepository.findAll();
            }
            
            // For each doctor, get their availability
            for (Doctor doctor : doctors) {
                List<String> availableSlots = getAvailableTimeSlotsForDoctor(doctor.getId(), date);
                
                // Filter by time slot if specified
                if (timeSlot != null && !timeSlot.trim().isEmpty()) {
                    availableSlots = filterSlotsByTimeOfDay(availableSlots, timeSlot);
                }
                
                if (!availableSlots.isEmpty()) {
                    DoctorAvailabilityDTO availability = new DoctorAvailabilityDTO();
                    availability.setDoctorId(doctor.getId());
                    availability.setDoctorName(doctor.getName());
                    availability.setSpecialty(doctor.getSpecialty());
                    availability.setDate(date);
                    availability.setAvailableSlots(availableSlots);
                    availability.setConsultationFee(doctor.getConsultationFee());
                    
                    availabilityList.add(availability);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting doctor availability: " + e.getMessage());
        }
        
        return availabilityList;
    }
    
    /**
     * Filter time slots by time of day (morning, afternoon, evening)
     */
    private List<String> filterSlotsByTimeOfDay(List<String> slots, String timeOfDay) {
        return slots.stream()
            .filter(slot -> {
                LocalTime time = LocalTime.parse(slot);
                switch (timeOfDay.toLowerCase()) {
                    case "morning":
                        return time.isBefore(LocalTime.of(12, 0));
                    case "afternoon":
                        return time.isAfter(LocalTime.of(11, 59)) && time.isBefore(LocalTime.of(17, 0));
                    case "evening":
                        return time.isAfter(LocalTime.of(16, 59));
                    default:
                        return true;
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get all doctors
     */
    public List<DoctorDTO> getAllDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        return doctors.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get doctors by specialty
     */
    public List<DoctorDTO> getDoctorsBySpecialty(String specialty) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyContainingIgnoreCase(specialty);
        return doctors.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get doctor by ID
     */
    public DoctorDTO getDoctorById(Long id) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(id);
        return doctorOpt.map(this::convertToDTO).orElse(null);
    }
    
    /**
     * Save new doctor
     */
    public DoctorDTO saveDoctor(DoctorDTO doctorDTO) {
        Doctor doctor = convertToEntity(doctorDTO);
        doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
        Doctor savedDoctor = doctorRepository.save(doctor);
        return convertToDTO(savedDoctor);
    }
    
    /**
     * Update doctor
     */
    public DoctorDTO updateDoctor(Long id, DoctorDTO doctorDTO) {
        Optional<Doctor> existingDoctorOpt = doctorRepository.findById(id);
        if (existingDoctorOpt.isEmpty()) {
            return null;
        }
        
        Doctor existingDoctor = existingDoctorOpt.get();
        updateDoctorFromDTO(existingDoctor, doctorDTO);
        Doctor updatedDoctor = doctorRepository.save(existingDoctor);
        return convertToDTO(updatedDoctor);
    }
    
    /**
     * Delete doctor
     */
    public boolean deleteDoctor(Long id) {
        if (doctorRepository.existsById(id)) {
            doctorRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * Check if email belongs to doctor with given ID
     */
    public boolean isDoctorEmail(String email, Long doctorId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        return doctorOpt.isPresent() && doctorOpt.get().getEmail().equals(email);
    }
    
    /**
     * Get doctor by email
     */
    public DoctorDTO getDoctorByEmail(String email) {
        Optional<Doctor> doctorOpt = doctorRepository.findByEmail(email);
        return doctorOpt.map(this::convertToDTO).orElse(null);
    }
    
    /**
     * Convert Doctor entity to DTO
     */
    private DoctorDTO convertToDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setName(doctor.getName());
        dto.setEmail(doctor.getEmail());
        dto.setSpecialty(doctor.getSpecialty());
        dto.setPhone(doctor.getPhone());
        dto.setQualification(doctor.getQualification());
        dto.setExperienceYears(doctor.getExperienceYears());
        dto.setConsultationFee(doctor.getConsultationFee());
        dto.setCreatedAt(doctor.getCreatedAt());
        dto.setUpdatedAt(doctor.getUpdatedAt());
        return dto;
    }
    
    /**
     * Convert DTO to Doctor entity
     */
    private Doctor convertToEntity(DoctorDTO dto) {
        Doctor doctor = new Doctor();
        doctor.setName(dto.getName());
        doctor.setEmail(dto.getEmail());
        doctor.setPassword(dto.getPassword());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setPhone(dto.getPhone());
        doctor.setQualification(dto.getQualification());
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setConsultationFee(dto.getConsultationFee());
        return doctor;
    }
    
    /**
     * Update doctor entity from DTO
     */
    private void updateDoctorFromDTO(Doctor doctor, DoctorDTO dto) {
        if (dto.getName() != null) doctor.setName(dto.getName());
        if (dto.getSpecialty() != null) doctor.setSpecialty(dto.getSpecialty());
        if (dto.getPhone() != null) doctor.setPhone(dto.getPhone());
        if (dto.getQualification() != null) doctor.setQualification(dto.getQualification());
        if (dto.getExperienceYears() != null) doctor.setExperienceYears(dto.getExperienceYears());
        if (dto.getConsultationFee() != null) doctor.setConsultationFee(dto.getConsultationFee());
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            doctor.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
    }
}
