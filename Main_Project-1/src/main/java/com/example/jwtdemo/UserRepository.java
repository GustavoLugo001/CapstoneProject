package com.example.jwtdemo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.password = :password")
    Optional<User> findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    
    @Query("SELECT u FROM User u WHERE u.adminCode = :adminCode")
    Optional<User> findByAdminCode(@Param("adminCode") String adminCode);
    
    @Query("SELECT u FROM User u WHERE u.admin.id = :adminId")
    List<User> findByAdminId(@Param("adminId") Long adminId);
}

