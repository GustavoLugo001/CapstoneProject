package com.example.jwtdemo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavedLocationRepository extends JpaRepository<SavedLocation, Long> {
    List<SavedLocation> findByAdminId(Long adminId);
}
