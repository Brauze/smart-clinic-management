package com.project.back_end.dto;

import com.project.back_end.model.Prescription;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PrescriptionDTO {
    
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;
    
    @NotBlank(message = "Patient name is required")
    private String patientName;
    
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    
    @NotBlank(message = "Doctor name is required")
    private String doctorName;
    
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    
    @NotNull(message = "Medications are required")
    private List<Prescription.Medication> medications;
    
    private String diagnosis;
    
    private String notes;
    
    private LocalDateTime nextVisit;
}