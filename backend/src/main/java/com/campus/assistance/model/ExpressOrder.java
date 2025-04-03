package com.campus.assistance.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "express_orders")
public class ExpressOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "issuer_id", nullable = false)
    private User issuer;
    
    @Column(name = "pickup_location", nullable = false)
    private String pickupLocation;
    
    @Column(name = "express_number")
    private String expressNumber;
    
    @Column(name = "package_size", nullable = false)
    private String packageSize; // 大/中/小
    
    @Column(name = "destination", nullable = false)
    private String destination;
    
    private LocalDateTime deadline;
    
    private BigDecimal reward;
    
    private String contact;
    
    private String status = "PENDING"; // PENDING/IN_PROGRESS/COMPLETED/CANCELLED
    
    @ManyToOne
    @JoinColumn(name = "taker_id")
    private User taker;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 