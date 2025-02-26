package com.example.jwtdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;

@RestController
@RequestMapping("/api/trees")
public class TreeController {

    @GetMapping("/test")
    public String testEndpoint() {
        return "This is a public test endpoint.";
    }
    
    @Autowired
    private TreeService treeService;

    @Autowired
    private UserRepository userRepository;
    
    private final TreeRepository treeRepository;
    
    @Autowired
    public TreeController(TreeRepository treeRepository) {
        this.treeRepository = treeRepository;
    }
    
    @PostMapping
    public ResponseEntity<Tree> addTree(@RequestBody Tree tree) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

        tree.setOwner(user);
        
        if (user.getRole().equals("ROLE_ADMIN")) {
            tree.setApprovalStatus("APPROVED");
            tree.setUserHasPermission(true);
        } else {
            tree.setApprovalStatus("PENDING");
            tree.setUserHasPermission(false);
        }

        if (tree.getLatitude() == null || tree.getLongitude() == null) {
            throw new RuntimeException("Location is required");
        }
        if (tree.getSpecies() == null) {
            tree.setSpecies("Unknown");
        }
        if (tree.getHealthStatus() == null) {
            tree.setHealthStatus("Good");
        }

        Tree savedTree = treeService.addTree(tree);
        return ResponseEntity.ok(savedTree);
    }


    @GetMapping
    public ResponseEntity<List<Tree>> getAllTrees() {
    	String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            // For ROLE_ADMIN: include trees created by the teacher and by their ROLE_USER
            List<User> students = userRepository.findByAdminId(currentUser.getId());
            List<Long> ownerIds = new ArrayList<>();
            ownerIds.add(currentUser.getId());
            for (User student : students) {
                ownerIds.add(student.getId());
            }
            List<Tree> trees = treeRepository.findByOwnerIdIn(ownerIds);
            return ResponseEntity.ok(trees);
        } else {
            // For ROLE_USER: return trees belonging to the user and to their admin (if available)
            List<Long> ownerIds = new ArrayList<>();
            ownerIds.add(currentUser.getId());
            if (currentUser.getAdmin() != null) {
                ownerIds.add(currentUser.getAdmin().getId());
            }
            List<Tree> trees = treeRepository.findByOwnerIdIn(ownerIds);
            return ResponseEntity.ok(trees);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tree> getTreeById(@PathVariable Long id) {
        Tree tree = treeService.getTreeById(id);
        return ResponseEntity.ok(tree);
    }

    @PutMapping("/{treeId}")
    public ResponseEntity<?> updateTree(@PathVariable Long treeId, @RequestBody Tree updatedTree) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                     .orElseThrow(() -> new RuntimeException("User not found"));

        Tree tree = treeService.updateTree(user.getId(), treeId, updatedTree);
        if (tree != null) {
            return ResponseEntity.ok(tree);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to update this tree");
        }
    }

    @DeleteMapping("/{treeId}")
    public ResponseEntity<?> deleteTree(@PathVariable Long treeId) {
        // Get the authenticated user's ID
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                     .orElseThrow(() -> new RuntimeException("User not found"));
        
        treeRepository.deleteById(treeId);
     // return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to delete this tree");
      return ResponseEntity.ok("Water line deleted successfully");


//        boolean deleted = treeService.deleteTree(user.getId(), treeId);
//        if (deleted) {
//            return ResponseEntity.ok("Tree deleted successfully");
//        } else {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to delete this tree");
//        }
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<String> approveTree(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only Admins can approve trees.");
        }

        Tree tree = treeService.getTreeById(id);
        tree.setApprovalStatus("APPROVED");  
        treeRepository.save(tree);  

        return ResponseEntity.ok("Tree approved successfully.");
    }

    @PutMapping("/deny/{id}")
    public ResponseEntity<String> denyTree(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can deny trees");
        }

        Tree tree = treeService.getTreeById(id);
        if (tree == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tree not found");
        }

        tree.setApprovalStatus("DENIED");
        treeRepository.save(tree);

        return ResponseEntity.ok("Tree denied!");
    }
    
    @PutMapping("/{treeId}/update-dates")
    public ResponseEntity<?> updateTreeDates(
        @PathVariable Long treeId,
        @RequestBody Map<String, Date> updates
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Tree tree = treeService.getTreeById(treeId);

        boolean isOwner = tree.getOwner().getId().equals(user.getId());
        boolean isAdmin = user.getRole().equals("ROLE_ADMIN");
        boolean hasPermission = Boolean.TRUE.equals(tree.getUserHasPermission());
        if (!(isOwner || isAdmin || hasPermission)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to update this tree");
        }

        if (updates.containsKey("last_watering_date")) {
            tree.setLastWateringDate(updates.get("last_watering_date"));
        }
        if (updates.containsKey("last_fertilization_date")) {
            tree.setLastFertilizationDate(updates.get("last_fertilization_date"));
        }
        if (updates.containsKey("planting_date")) {
            tree.setPlantingDate(updates.get("planting_date"));
        }

        treeService.addTree(tree); 

        return ResponseEntity.ok("Dates updated successfully");
    }
}
