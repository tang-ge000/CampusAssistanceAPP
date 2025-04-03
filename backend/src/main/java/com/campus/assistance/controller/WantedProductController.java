package com.campus.assistance.controller;

import com.campus.assistance.dto.ProductDTO;
import com.campus.assistance.enums.ProductStatus;
import com.campus.assistance.enums.ProductType;
import com.campus.assistance.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/wanted")
@RequiredArgsConstructor
public class WantedProductController {

    private final ProductService productService;

    /**
     * 发布求购信息
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createWantedProduct(@Valid @RequestBody ProductDTO productDTO) {
        Long userId = getCurrentUserId();
        
        // 设置求购商品类型
        productDTO.setType(ProductType.WANTED);
        productDTO.setStatus(ProductStatus.ACTIVE);
        
        // 如果价格字段为空，但有最低价和最高价，则使用最低价作为显示价格
        if (productDTO.getPrice() == null && productDTO.getMinPrice() != null) {
            productDTO.setPrice(productDTO.getMinPrice());
        }
        
        // 设置过期时间（默认7天）
        if (productDTO.getExpiryTime() == null) {
            productDTO.setExpiryTime(LocalDateTime.now().plusDays(7));
        }
        
        ProductDTO createdProduct = productService.createProduct(productDTO, userId);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    /**
     * 获取所有求购商品（分页）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllWantedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String urgency) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        // 调用特定的服务方法来获取求购商品
        Page<ProductDTO> productPage = productService.getWantedProducts(category, urgency, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("products", productPage.getContent());
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户自己的求购商品
     */
    @GetMapping("/user")
    public ResponseEntity<List<ProductDTO>> getUserWantedProducts(
            @RequestParam(required = false) String status) {
        Long userId = getCurrentUserId();
        
        // 调用特定的服务方法来获取用户的求购商品
        List<ProductDTO> products = productService.getUserWantedProducts(userId, status);
        return ResponseEntity.ok(products);
    }

    /**
     * 根据ID获取求购商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getWantedProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        
        // 验证是否为求购商品
        if (product.getType() != ProductType.WANTED) {
            return ResponseEntity.notFound().build();
        }
        
        // 增加浏览次数
        productService.incrementViewCount(id);
        return ResponseEntity.ok(product);
    }

    /**
     * 更新求购商品信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateWantedProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        Long userId = getCurrentUserId();
        
        // 确保类型不变
        productDTO.setType(ProductType.WANTED);
        
        // 如果价格字段为空，但有最低价和最高价，则使用最低价作为显示价格
        if (productDTO.getPrice() == null && productDTO.getMinPrice() != null) {
            productDTO.setPrice(productDTO.getMinPrice());
        }
        
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO, userId);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * 更新求购商品状态
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductDTO> updateWantedProductStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusMap) {
        Long userId = getCurrentUserId();
        String statusStr = statusMap.get("status");
        
        if (statusStr == null || statusStr.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        ProductStatus status = ProductStatus.valueOf(statusStr.toUpperCase());
        ProductDTO updatedProduct = productService.updateProductStatus(id, status, userId);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * 删除求购商品
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWantedProduct(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        productService.deleteProduct(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 搜索求购商品
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchWantedProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        // 调用特定的服务方法来搜索求购商品
        Page<ProductDTO> productPage = productService.searchWantedProducts(keyword, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("products", productPage.getContent());
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        return Long.parseLong(authentication.getName());
    }
} 