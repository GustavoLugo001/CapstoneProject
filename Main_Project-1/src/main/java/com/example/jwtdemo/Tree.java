package com.example.jwtdemo;

import jakarta.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "trees")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Tree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "approval_status", nullable = false)
    private String approvalStatus = "PENDING";

    
    public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	@Column(name = "user_has_permission", nullable = false)
    private Boolean userHasPermission = false;

    @Temporal(TemporalType.DATE)
    @Column(name = "last_fertilization_date")
    private Date lastFertilizationDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "next_fertilization_date")
    private Date nextFertilizationDate;
   
    @Temporal(TemporalType.DATE)
    @Column(name = "next_watering_date")
    private Date nextWateringDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "last_watering_date") // or "last_watering_date" if that's your choice
    private Date lastWateringDate; // tree was last watered
    
    @Column(name = "species", nullable = false)
    private String species; 

    @Column(name = "health_status", nullable = false)
    private String healthStatus; // e.g., Healthy, Diseased

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "height")
    private Float height; // Height of the tree in meters

    @Column(name = "soil_moisture_level")
    private Float soilMoistureLevel;

    @Column(name = "temperature")
    private Float temperature;

    @Column(name = "humidity")
    private Float humidity;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "planting_date")
    private Date plantingDate;


    @Column(name = "health_note", columnDefinition = "TEXT")
    private String healthNote;


    public String getHealthNote() {
        return healthNote;
    }

    public void setHealthNote(String healthNote) {
        this.healthNote = healthNote;
    }


    // Getter and Setter for plantingDate
    public Date getPlantingDate() {
        return plantingDate;
    }

    public void setPlantingDate(Date plantingDate) {
        this.plantingDate = plantingDate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false) 
    @JsonIgnore
    private User owner; 

    // Getters and Setters for other fields

    public Boolean getUserHasPermission() {
        return userHasPermission;
    }

    public void setUserHasPermission(Boolean userHasPermission) {
        this.userHasPermission = userHasPermission;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public Date getLastWateringDate() {
        return lastWateringDate;
    }

    public void setLastWateringDate(Date lastWateringDate) {
        this.lastWateringDate = lastWateringDate;
    }

    public Date getNextFertilizationDate() {
        return nextFertilizationDate;
    }

    public void setNextFertilizationDate(Date nextFertilizationDate) {
        this.nextFertilizationDate = nextFertilizationDate;
    }

    public Date getNextWateringDate() {
        return nextWateringDate;
    }

    public void setNextWateringDate(Date nextWateringDate) {
        this.nextWateringDate = nextWateringDate;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
    
    @ManyToMany
    @JoinTable(
        name = "tree_permissions",
        joinColumns = @JoinColumn(name = "tree_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> usersWithAccess = new HashSet<>();
    
    public Long getAdminId() {
        return owner != null ? owner.getId() : null;
    }

    public String getLocation() {
        return this.latitude + ", " + this.longitude;
    }

    public String getName() {
        return species;
    }

    public void setName(String name) {
        this.species = name;
    }

    // If you want to support setting location via a single string (e.g. "lat, lng")
    public void setLocation(String location) {
        if (location != null && location.contains(",")) {
            String[] parts = location.split(",");
            try {
                this.latitude = Double.parseDouble(parts[0].trim());
                this.longitude = Double.parseDouble(parts[1].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid location format. Expected format: 'latitude, longitude'");
            }
        } else {
            throw new IllegalArgumentException("Invalid location format. Expected format: 'latitude, longitude'");
        }
    }

    public Date getLastFertilizationDate() {
        return lastFertilizationDate;
    }

    public void setLastFertilizationDate(Date lastFertilizationDate) {
        this.lastFertilizationDate = lastFertilizationDate;
    }
}
