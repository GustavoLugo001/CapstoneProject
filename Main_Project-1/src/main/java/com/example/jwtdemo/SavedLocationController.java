package com.example.jwtdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/saved-locations")
public class SavedLocationController {

    @Autowired
    private SavedLocationRepository savedLocationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @PutMapping("/approve/{id}")
    public ResponseEntity<String> approveSavedLocation(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only admins can approve Saved Location.");
        }

        SavedLocation savedLocation = savedLocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Saved Location not found"));

        savedLocation.setApprovalStatus("APPROVED");
        savedLocationRepository.save(savedLocation);

        return ResponseEntity.ok("Saved Location approved successfully.");
    }

    @PutMapping("/deny/{id}")
    public ResponseEntity<String> denySavedLocation(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only admins can deny Saved Location.");
        }

        SavedLocation savedLocation = savedLocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Saved Location not found"));

        savedLocation.setApprovalStatus("DENIED");
        savedLocationRepository.save(savedLocation);

        return ResponseEntity.ok("Saved Location denied.");
    }

    
    @GetMapping
    public ResponseEntity<List<SavedLocation>> getSavedLocations() {
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
        
        List<SavedLocation> locations = savedLocationRepository.findByAdminId(adminId);
        return ResponseEntity.ok(locations);
    }
    
    @PostMapping
    public ResponseEntity<SavedLocation> addSavedLocation(@RequestBody SavedLocation savedLocation) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
        	savedLocation.setApprovalStatus("APPROVED");
        } else {
        	savedLocation.setApprovalStatus("PENDING");
        }
        Long adminId = currentUser.getRole().equals("ROLE_ADMIN")
        	    ? currentUser.getId()
        	    : (currentUser.getAdmin() != null ? currentUser.getAdmin().getId() : currentUser.getId());
        savedLocation.setAdminId(adminId );
        SavedLocation saved = savedLocationRepository.save(savedLocation);
        return ResponseEntity.ok(saved);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSavedLocation(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        savedLocationRepository.deleteById(id);
        return ResponseEntity.ok("Saved location deleted successfully");
    }

}
