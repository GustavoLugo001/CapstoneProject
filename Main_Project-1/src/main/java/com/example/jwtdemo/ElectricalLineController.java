package com.example.jwtdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
//RequestMapping will allow them to be called to be called by the api.js, however, it must pass the securityFIlter in SecurityConfig.
@RestController
@RequestMapping("/api/electrical-lines") //the directory to call each function have addtional in order to make more specific calls.
public class ElectricalLineController {

    @Autowired
    private ElectricalLineRepository electricalLineRepository;
    
    @Autowired
    private UserRepository userRepository;

    //approval is more for ROLE_USER to allow ROLE_ADMIN the ability to approve.
    
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

    //Deny is more for ROLE_USER to allow ROLE_ADMIN the ability to deny.
    
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


    //This allows the ability to get the ElectricalLine  based on the user based ont he admin as well.
    @GetMapping
    public ResponseEntity<List<ElectricalLine>> getElectricalLines() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        Long adminId;
        if ("ROLE_ADMIN".equals(user.getRole())) {
            adminId = user.getId();
        } else {
            adminId = (user.getAdmin() != null) ? user.getAdmin().getId() : user.getId();
        }
        
        List<ElectricalLine> lines = electricalLineRepository.findByAdminId(adminId);
        return ResponseEntity.ok(lines);
    }

    //This allows to add of ElecricalLine to be added when using the front-end this allows the communication.
    //it also has teh feature of allowing PENDING for ROLE_USER and APPROVAL for ROLE_ADMIN
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
    //based on the ID the WaterLine will be deleted.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteElectricalLine(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        electricalLineRepository.deleteById(id);
        return ResponseEntity.ok("Electrical line deleted successfully");
    }

}

