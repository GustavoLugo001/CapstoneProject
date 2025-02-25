package com.example.jwtdemo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WaterLineRepository extends JpaRepository<WaterLine, Long> {
    List<WaterLine> findByAdminId(Long adminId);
}
