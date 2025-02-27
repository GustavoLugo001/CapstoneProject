package com.example.jwtdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;


	@Service
	public class TreeService {

		
	    @Autowired
	    private TreeRepository treeRepository;
	    
	    @Autowired
	    private UserRepository userRepository;

	    public Tree addTree(Tree tree) {
	        return treeRepository.save(tree);
	    }

	    public List<Tree> getAllTrees() {
	        return treeRepository.findAll();
	    }

	    public Tree getTreeById(Long id) {
	        return treeRepository.findById(id).orElseThrow(() -> new RuntimeException("Tree not found"));
	    }

	    public Tree updateTree(Long adminId, Long treeId, Tree updatedTree) {
	        return treeRepository.findById(treeId).map(tree -> {
	            if (tree.getAdminId().equals(adminId)) {
	                tree.setName(updatedTree.getName()); 
	                if (updatedTree.getLocation() != null && !updatedTree.getLocation().trim().isEmpty()&& updatedTree.getLocation().contains(",")) {
	                        String[] parts = updatedTree.getLocation().split(",");
	                        if (parts.length == 2) {
	                            try {
	                                Double.parseDouble(parts[0].trim());
	                                Double.parseDouble(parts[1].trim());
	                                tree.setLocation(updatedTree.getLocation());
	                            } catch (NumberFormatException e) {
	                                System.err.println("Invalid location numbers: " + updatedTree.getLocation());
	                            }
	                        }
	                    }
	                    
	                tree.setHealthStatus(updatedTree.getHealthStatus());
	                tree.setHealthNote(updatedTree.getHealthNote());
	                return treeRepository.save(tree);
	            }
	            return null;
	        }).orElse(null);
	    }


	    public boolean deleteTree(Long adminId, Long treeId) {
	        Optional<Tree> treeOptional = treeRepository.findById(treeId);
	        if (treeOptional.isPresent() && treeOptional.get().getAdminId().equals(adminId)) {
	            treeRepository.deleteById(treeId);
	            return true;
	        }
	        return false;
	    }


	    // public boolean userHasPermission(Long userId, Long treeId) {
	    //     Integer count = treeRepository.countUserPermissions(userId, treeId);
	    //     return count != null && count > 0;
	    // }
	    
	    public void updateTreeCareSchedules(Tree tree) {
	        if (tree.getLastWateringDate() != null && tree.getLastFertilizationDate() != null) {
	            LocalDate lastWateredLocalDate = tree.getLastWateringDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	            LocalDate lastFertilizedLocalDate = tree.getLastFertilizationDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

	            LocalDate nextWateringLocalDate;
	            LocalDate nextFertilizationLocalDate;

	            if (tree.getSpecies().equalsIgnoreCase("Avocado")) {
	                nextWateringLocalDate = lastWateredLocalDate.plusDays(3);
	                nextFertilizationLocalDate = lastFertilizedLocalDate.plusWeeks(2);
	            } else {
	                nextWateringLocalDate = lastWateredLocalDate.plusDays(7);
	                nextFertilizationLocalDate = lastFertilizedLocalDate.plusMonths(1);
	            }

	            tree.setNextWateringDate(Date.from(nextWateringLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
	            tree.setNextFertilizationDate(Date.from(nextFertilizationLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

	            treeRepository.save(tree);
	        }
	    }


}
