package com.project.back_end.mvc;

import com.project.back_end.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DashboardController {
    
    @Autowired
    private TokenService tokenService;
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        try {
            if (tokenService.validateToken(token)) {
                String role = tokenService.extractRole(token);
                if ("ADMIN".equals(role)) {
                    return "admin/adminDashboard";
                }
            }
        } catch (Exception e) {
            // Log error
        }
        return "redirect:/";
    }
    
    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        try {
            if (tokenService.validateToken(token)) {
                String role = tokenService.extractRole(token);
                if ("DOCTOR".equals(role)) {
                    return "doctor/doctorDashboard";
                }
            }
        } catch (Exception e) {
            // Log error
        }
        return "redirect:/";
    }
    
    @GetMapping("/patientDashboard/{token}")
    public String patientDashboard(@PathVariable String token) {
        try {
            if (tokenService.validateToken(token)) {
                String role = tokenService.extractRole(token);
                if ("PATIENT".equals(role)) {
                    return "patient/patientDashboard";
                }
            }
        } catch (Exception e) {
            // Log error
        }
        return "redirect:/";
    }
}