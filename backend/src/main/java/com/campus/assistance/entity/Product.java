package com.campus.assistance.entity;

import com.campus.assistance.enums.ProductStatus;
import com.campus.assistance.enums.ProductType;
import com.campus.assistance.model.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal originalPrice;
    
    @Column(name = "min_price", precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 10, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "condition_level", nullable = false)
    private String conditionLevel;

    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "urgency_level")
    private String urgencyLevel;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> images;

    @ElementCollection
    @CollectionTable(name = "product_trade_methods", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "trade_method")
    private List<String> tradeMethods;

    @Column(name = "contact_info")
    private String contactInfo;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductType type;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;
    
    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @PrePersist
    protected void onCreate() {
        lastModified = LocalDateTime.now();
        if (publishTime == null && status == ProductStatus.ACTIVE) {
            publishTime = LocalDateTime.now();
        }
        if (type == null) {
            type = ProductType.NORMAL;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModified = LocalDateTime.now();
        if (status == ProductStatus.ACTIVE && publishTime == null) {
            publishTime = LocalDateTime.now();
        }
    }
} 