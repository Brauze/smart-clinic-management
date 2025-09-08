package com.project.back_end.service;

import com.project.back_end.model.Admin;
import com.project.back_end.repository.AdminRepository;
import com.project.back_end.dto.LoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private TokenService tokenService;
    
    public Admin createAdmin(Admin admin) {
        if (adminRepository.existsByUsername(admin.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (adminRepository.existsByEmail(admin.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        return adminRepository.save(admin);
    }
    
    public Map<String, Object> validateAdminLogin(LoginDTO loginDTO) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Admin> adminOpt = adminRepository.findByEmail(loginDTO.getEmail());
        if (adminOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Invalid email or password");
            return response;
        }
        
        Admin admin = adminOpt.get();
        if (!admin.getPassword().equals(loginDTO.getPassword())) {
            response.put("success", false);
            response.put("message", "Invalid email or password");
            return response;
        }
        
        String token = tokenService.generateTokenWithRole(admin.getEmail(), "ADMIN");
        response.put("success", true);
        response.put("token", token);
        response.put("admin", admin);
        response.put("message", "Login successful");
        
        return response;
    }
    
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }
    
    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }
}