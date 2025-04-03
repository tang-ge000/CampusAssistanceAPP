package com.campus.assistance.model;

import com.campus.assistance.entity.Product;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "purchase_records")
public class PurchaseRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;
    
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    
    private BigDecimal price;
    
    private String status = "COMPLETED";
    
    @Column(name = "purchase_time")
    private LocalDateTime purchaseTime;
    
    @PrePersist
    protected void onCreate() {
        purchaseTime = LocalDateTime.now();
    }
} 