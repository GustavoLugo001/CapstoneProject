package com.example.jwtdemo;

import jakarta.persistence.*;

//Here we have teh getters and setters for ElectricalLine.
//electrical_lines is a table that electriline will set and get from  in order to connetc with the database.

@Entity
@Table(name = "electrical_lines")
public class ElectricalLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "admin_id", nullable = false)
    private Long adminId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "line_geometry", columnDefinition = "TEXT", nullable = false)
    private String lineGeometry;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "approval_status", nullable = false)
    private String approvalStatus = "PENDING";

    
    public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAdminId() {
		return adminId;
	}

	public void setAdminId(Long adminId) {
		this.adminId = adminId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLineGeometry() {
		return lineGeometry;
	}

	public void setLineGeometry(String lineGeometry) {
		this.lineGeometry = lineGeometry;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    // Getters and setters...
    
}
