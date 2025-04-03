package com.campus.assistance.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "browse_history")
public class BrowseHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "item_type", nullable = false)
    private String itemType; // 'product' or 'express'
    
    @Column(name = "item_id", nullable = false)
    private Long itemId;
    
    @Column(name = "browse_time")
    private LocalDateTime browseTime;
    
    @PrePersist
    protected void onCreate() {
        browseTime = LocalDateTime.now();
    }
} 