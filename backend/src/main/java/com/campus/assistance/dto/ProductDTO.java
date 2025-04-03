package com.campus.assistance.dto;

import com.campus.assistance.enums.ProductStatus;
import com.campus.assistance.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    
    private Long id;
    
    @NotBlank(message = "商品标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    private String title;
    
    @NotBlank(message = "分类不能为空")
    private String category;
    
    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须为正数")
    private BigDecimal price;
    
    private BigDecimal originalPrice;
    
    // 求购商品的最低价格
    private BigDecimal minPrice;
    
    // 求购商品的最高价格
    private BigDecimal maxPrice;
    
    @NotBlank(message = "商品成色不能为空")
    private String conditionLevel;
    
    @NotBlank(message = "商品描述不能为空")
    private String description;
    
    // 求购商品的紧急程度
    private String urgencyLevel;
    
    @NotNull(message = "至少上传一张商品图片")
    @Size(min = 1, max = 5, message = "图片数量必须在1-5张之间")
    private List<String> images;
    
    @NotNull(message = "请选择至少一种交易方式")
    private List<String> tradeMethods;
    
    private String contactInfo;
    
    private ProductStatus status;
    
    // 商品类型：普通商品或求购商品
    private ProductType type;
    
    private Long userId;
    
    private String userName;
    
    private LocalDateTime publishTime;
    
    private LocalDateTime lastModified;
    
    // 求购商品的过期时间
    private LocalDateTime expiryTime;
    
    private Integer viewCount;
} 