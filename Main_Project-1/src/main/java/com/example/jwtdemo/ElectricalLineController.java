package com.example.jwtdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/electrical-lines")
public class ElectricalLineController {

    @Autowired
    private ElectricalLineRepository electricalLineRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @PutMapping("/approve/{id}")
    public ResponseEntity<String> approveElectricalLine(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only admins can approve electrical lines.");
        }

        ElectricalLine electricalLine = electricalLineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Electrical line not found"));

        electricalLine.setApprovalStatus("APPROVED");
        electricalLineRepository.save(electricalLine);

        return ResponseEntity.ok("Electrical line approved successfully.");
    }

    @PutMapping("/deny/{id}")
    public ResponseEntity<String> denyElectricalLine(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only admins can deny electrical lines.");
        }

        ElectricalLine electricalLine = electricalLineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Electrical line not found"));

        electricalLine.setApprovalStatus("DENIED");
        electricalLineRepository.save(electricalLine);

        return ResponseEntity.ok("Electrical line denied.");
    }


    
    @GetMapping
    public ResponseEntity<List<ElectricalLine>> getElectricalLines() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        Long adminId;
        if ("ROLE_ADMIN".equals(user.getRole())) {
            adminId = user.getId();
        } else {
            // For students, use the admin's id if available
            adminId = (user.getAdmin() != null) ? user.getAdmin().getId() : user.getId();
        }
        
        List<ElectricalLine> lines = electricalLineRepository.findByAdminId(adminId);
        return ResponseEntity.ok(lines);
    }
    
    @PostMapping
    public ResponseEntity<ElectricalLine> addElectricalLine(@RequestBody ElectricalLine electricalLine) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
        	electricalLine.setApprovalStatus("APPROVED");
        } else {
        	electricalLine.setApprovalStatus("PENDING");
        }
        Long adminId = currentUser.getRole().equals("ROLE_ADMIN")
        	    ? currentUser.getId()
        	    : (currentUser.getAdmin() != null ? currentUser.getAdmin().getId() : currentUser.getId());
        electricalLine.setAdminId(adminId);
        ElectricalLine savedLine = electricalLineRepository.save(electricalLine);
        return ResponseEntity.ok(savedLine);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteElectricalLine(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        electricalLineRepository.deleteById(id);
        return ResponseEntity.ok("Electrical line deleted successfully");
    }

}

