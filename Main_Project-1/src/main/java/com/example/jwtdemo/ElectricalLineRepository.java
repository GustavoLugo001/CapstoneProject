package com.example.jwtdemo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ElectricalLineRepository extends JpaRepository<ElectricalLine, Long> {
    List<ElectricalLine> findByAdminId(Long adminId);
}
