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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductController {

    private final ProductService productService;

    /**
     * 创建新商品
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            Long userId = getCurrentUserId();
            
            // 设置默认类型为普通商品
            if (productDTO.getType() == null) {
                productDTO.setType(ProductType.NORMAL);
            }
            
            // 设置默认状态为活跃
            if (productDTO.getStatus() == null) {
                productDTO.setStatus(ProductStatus.ACTIVE);
            }
            
            ProductDTO createdProduct = productService.createProduct(productDTO, userId);
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            throw e; // 重新抛出异常，交给全局异常处理器
        }
    }

    /**
     * 获取所有商品（分页）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<ProductDTO> productPage = productService.getAllActiveProducts(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("products", productPage.getContent());
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取指定分类的商品
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<ProductDTO> productPage = productService.getProductsByCategory(category, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("products", productPage.getContent());
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取指定状态的商品（仅供用户自己查看）
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProductDTO>> getProductsByStatus(@PathVariable String status) {
        Long userId = getCurrentUserId();
        ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
        List<ProductDTO> products = productService.getProductsByUserAndStatus(userId, productStatus);
        return ResponseEntity.ok(products);
    }

    /**
     * 获取用户自己的所有商品
     */
    @GetMapping("/user")
    public ResponseEntity<List<ProductDTO>> getUserProducts(
            @RequestParam(required = false) String status) {
        Long userId = getCurrentUserId();
        
        if (status != null && !status.isEmpty()) {
            ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(productService.getProductsByUserAndStatus(userId, productStatus));
        } else {
            return ResponseEntity.ok(productService.getProductsByUser(userId));
        }
    }

    /**
     * 根据ID获取商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        // 增加浏览次数
        productService.incrementViewCount(id);
        return ResponseEntity.ok(product);
    }

    /**
     * 更新商品信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        Long userId = getCurrentUserId();
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO, userId);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * 更新商品状态
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductDTO> updateProductStatus(
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
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        productService.deleteProduct(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 搜索商品
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<ProductDTO> productPage = productService.searchProducts(keyword, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("products", productPage.getContent());
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 筛选商品
     */
    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> filterProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<ProductDTO> productPage = productService.filterProducts(category, condition, minPrice, maxPrice, pageable);
        
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