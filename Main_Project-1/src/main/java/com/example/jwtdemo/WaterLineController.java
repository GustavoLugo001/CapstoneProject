package com.example.jwtdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/water-lines")
public class WaterLineController {

    @Autowired
    private WaterLineRepository waterLineRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @PutMapping("/approve/{id}")
    public ResponseEntity<String> approveWaterLine(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only admins can approve water lines.");
        }

        WaterLine waterLine = waterLineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Water line not found"));

        waterLine.setApprovalStatus("APPROVED");
        waterLineRepository.save(waterLine);

        return ResponseEntity.ok("Water line approved successfully.");
    }

    @PutMapping("/deny/{id}")
    public ResponseEntity<String> denyWaterLine(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only admins can deny water lines.");
        }

        WaterLine waterLine = waterLineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Water line not found"));

        waterLine.setApprovalStatus("DENIED");
        waterLineRepository.save(waterLine);

        return ResponseEntity.ok("Water line denied.");
    }

    
    // Get all water lines for the current admin 
    @GetMapping
    public ResponseEntity<List<WaterLine>> getWaterLines() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        Long adminId;
        if ("ROLE_ADMIN".equals(user.getRole())) {
            adminId = user.getId();
        } else {
            // For ROLE_USER, use the admin's id if available
            adminId = (user.getAdmin() != null) ? user.getAdmin().getId() : user.getId();
        }
        
        // current user is the admin
        List<WaterLine> lines = waterLineRepository.findByAdminId(adminId);
        return ResponseEntity.ok(lines);
    }
    
    // Add a new water line (assigning it to the current admin)
    @PostMapping
    public ResponseEntity<WaterLine> addWaterLine(@RequestBody WaterLine waterLine) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            waterLine.setApprovalStatus("APPROVED");
        } else {
            waterLine.setApprovalStatus("PENDING");
        }
        Long adminId = currentUser.getRole().equals("ROLE_ADMIN")
        	    ? currentUser.getId()
        	    : (currentUser.getAdmin() != null ? currentUser.getAdmin().getId() : currentUser.getId());
        waterLine.setAdminId(adminId);
        WaterLine savedLine = waterLineRepository.save(waterLine);
        return ResponseEntity.ok(savedLine);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWaterLine(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        waterLineRepository.deleteById(id);
        return ResponseEntity.ok("Water line deleted successfully");
    }

}
