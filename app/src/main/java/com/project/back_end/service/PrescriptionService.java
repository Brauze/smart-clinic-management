package com.project.back_end.service;

import com.project.back_end.model.Prescription;
import com.project.back_end.repository.PrescriptionRepository;
import com.project.back_end.dto.PrescriptionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PrescriptionService {
    
    @Autowired
    private PrescriptionRepository prescriptionRepository;
    
    public Prescription createPrescription(PrescriptionDTO prescriptionDTO) {
        Prescription prescription = new Prescription();
        prescription.setAppointmentId(prescriptionDTO.getAppointmentId());
        prescription.setPatientName(prescriptionDTO.getPatientName());
        prescription.setPatientId(prescriptionDTO.getPatientId());
        prescription.setDoctorName(prescriptionDTO.getDoctorName());
        prescription.setDoctorId(prescriptionDTO.getDoctorId());
        prescription.setPrescriptionDate(LocalDateTime.now());
        prescription.setMedications(prescriptionDTO.getMedications());
        prescription.setDiagnosis(prescriptionDTO.getDiagnosis());
        prescription.setNotes(prescriptionDTO.getNotes());
        prescription.setNextVisit(prescriptionDTO.getNextVisit());
        
        return prescriptionRepository.save(prescription);
    }
    
    public List<Prescription> getPrescriptionsByPatientId(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }
    
    public List<Prescription> getPrescriptionsByDoctorId(Long doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId);
    }
    
    public Optional<Prescription> getPrescriptionById(String id) {
        return prescriptionRepository.findById(id);
    }
    
    public List<Prescription> getPrescriptionsByAppointmentId(Long appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId);
    }
    
    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }
    
    public void deletePrescription(String id) {
        prescriptionRepository.deleteById(id);
    }
}