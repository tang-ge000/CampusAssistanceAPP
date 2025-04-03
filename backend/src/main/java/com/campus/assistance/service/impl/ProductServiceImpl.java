package com.campus.assistance.service.impl;

import com.campus.assistance.dto.ProductDTO;
import com.campus.assistance.entity.Product;
import com.campus.assistance.model.User;
import com.campus.assistance.enums.ProductStatus;
import com.campus.assistance.enums.ProductType;
import com.campus.assistance.exception.ResourceNotFoundException;
import com.campus.assistance.exception.UnauthorizedException;
import com.campus.assistance.repository.ProductRepository;
import com.campus.assistance.repository.UserRepository;
import com.campus.assistance.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    
            Product product = Product.builder()
                    .title(productDTO.getTitle())
                    .category(productDTO.getCategory())
                    .price(productDTO.getPrice())
                    .originalPrice(productDTO.getOriginalPrice())
                    .minPrice(productDTO.getMinPrice())
                    .maxPrice(productDTO.getMaxPrice())
                    .conditionLevel(productDTO.getConditionLevel())
                    .description(productDTO.getDescription())
                    .images(productDTO.getImages())
                    .tradeMethods(productDTO.getTradeMethods())
                    .contactInfo(productDTO.getContactInfo())
                    .user(user)
                    .status(productDTO.getStatus() != null ? productDTO.getStatus() : ProductStatus.ACTIVE)
                    .type(productDTO.getType() != null ? productDTO.getType() : ProductType.NORMAL)
                    .urgencyLevel(productDTO.getUrgencyLevel())
                    .expiryTime(productDTO.getExpiryTime())
                    .publishTime(productDTO.getStatus() == ProductStatus.ACTIVE ? LocalDateTime.now() : null)
                    .lastModified(LocalDateTime.now())
                    .viewCount(0)
                    .build();
    
            System.out.println("保存商品前: " + product);
            Product savedProduct = productRepository.save(product);
            System.out.println("保存商品后: " + savedProduct);
            return convertToDTO(savedProduct);
        } catch (Exception e) {
            System.err.println("创建商品时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));
        
        return convertToDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, Long userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));
        
        // 检查权限
        if (!product.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("无权修改该商品");
        }
        
        // 更新商品信息
        product.setTitle(productDTO.getTitle());
        product.setCategory(productDTO.getCategory());
        product.setPrice(productDTO.getPrice());
        product.setOriginalPrice(productDTO.getOriginalPrice());
        product.setMinPrice(productDTO.getMinPrice());
        product.setMaxPrice(productDTO.getMaxPrice());
        product.setConditionLevel(productDTO.getConditionLevel());
        product.setDescription(productDTO.getDescription());
        product.setImages(productDTO.getImages());
        product.setTradeMethods(productDTO.getTradeMethods());
        product.setContactInfo(productDTO.getContactInfo());
        product.setUrgencyLevel(productDTO.getUrgencyLevel());
        product.setExpiryTime(productDTO.getExpiryTime());
        
        // 如果状态发生变化
        if (productDTO.getStatus() != null && product.getStatus() != productDTO.getStatus()) {
            product.setStatus(productDTO.getStatus());
            if (productDTO.getStatus() == ProductStatus.ACTIVE && product.getPublishTime() == null) {
                product.setPublishTime(LocalDateTime.now());
            }
        }
        
        product.setLastModified(LocalDateTime.now());
        Product updatedProduct = productRepository.save(product);
        
        return convertToDTO(updatedProduct);
    }

    @Override
    @Transactional
    public ProductDTO updateProductStatus(Long id, ProductStatus status, Long userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));
        
        // 检查权限
        if (!product.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("无权修改该商品");
        }
        
        product.setStatus(status);
        product.setLastModified(LocalDateTime.now());
        
        // 如果状态变为ACTIVE，并且以前没有发布时间，设置发布时间
        if (status == ProductStatus.ACTIVE && product.getPublishTime() == null) {
            product.setPublishTime(LocalDateTime.now());
        }
        
        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, Long userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));
        
        // 检查权限
        if (!product.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("无权删除该商品");
        }
        
        productRepository.delete(product);
    }

    @Override
    public Page<ProductDTO> getAllActiveProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
        return products.map(this::convertToDTO);
    }

    @Override
    public Page<ProductDTO> getProductsByCategory(String category, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryAndStatus(category, ProductStatus.ACTIVE, pageable);
        return products.map(this::convertToDTO);
    }

    @Override
    public Page<ProductDTO> searchProducts(String keyword, Pageable pageable) {
        Page<Product> products = productRepository.searchByKeyword(keyword, ProductStatus.ACTIVE, pageable);
        return products.map(this::convertToDTO);
    }

    @Override
    public Page<ProductDTO> filterProducts(String category, String condition, Double minPrice, Double maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryAndConditionLevelAndStatusAndPriceBetween(
                category, condition, ProductStatus.ACTIVE, minPrice, maxPrice, pageable);
        return products.map(this::convertToDTO);
    }

    @Override
    public List<ProductDTO> getProductsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        List<Product> products = productRepository.findByUser(user);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByUserAndStatus(Long userId, ProductStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        List<Product> products = productRepository.findByUserAndStatus(user, status);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));
        
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
    }
    
    /**
     * 将商品实体转换为DTO
     */
    @Override
    public ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .title(product.getTitle())
                .category(product.getCategory())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .minPrice(product.getMinPrice())
                .maxPrice(product.getMaxPrice())
                .conditionLevel(product.getConditionLevel())
                .description(product.getDescription())
                .images(product.getImages())
                .tradeMethods(product.getTradeMethods())
                .contactInfo(product.getContactInfo())
                .status(product.getStatus())
                .userId(product.getUser().getId())
                .userName(product.getUser().getUsername())
                .publishTime(product.getPublishTime())
                .lastModified(product.getLastModified())
                .viewCount(product.getViewCount())
                .type(product.getType())
                .urgencyLevel(product.getUrgencyLevel())
                .expiryTime(product.getExpiryTime())
                .build();
    }
    
    @Override
    public Page<ProductDTO> getWantedProducts(String category, String urgency, Pageable pageable) {
        // 如果有分类筛选和紧急程度筛选
        if (category != null && !category.isEmpty() && urgency != null && !urgency.isEmpty()) {
            return productRepository.findByTypeAndCategoryAndUrgencyLevelAndStatus(
                    ProductType.WANTED, category, urgency, ProductStatus.ACTIVE, pageable)
                    .map(this::convertToDTO);
        }
        // 如果只有分类筛选
        else if (category != null && !category.isEmpty()) {
            return productRepository.findByTypeAndCategoryAndStatus(
                    ProductType.WANTED, category, ProductStatus.ACTIVE, pageable)
                    .map(this::convertToDTO);
        }
        // 如果只有紧急程度筛选
        else if (urgency != null && !urgency.isEmpty()) {
            return productRepository.findByTypeAndUrgencyLevelAndStatus(
                    ProductType.WANTED, urgency, ProductStatus.ACTIVE, pageable)
                    .map(this::convertToDTO);
        }
        // 如果没有筛选条件
        else {
            return productRepository.findByTypeAndStatus(ProductType.WANTED, ProductStatus.ACTIVE, pageable)
                    .map(this::convertToDTO);
        }
    }
    
    @Override
    public Page<ProductDTO> searchWantedProducts(String keyword, Pageable pageable) {
        return productRepository.searchWantedProductsByKeyword(keyword, pageable)
                .map(this::convertToDTO);
    }
    
    @Override
    public List<ProductDTO> getUserWantedProducts(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 如果有状态筛选
        if (status != null && !status.isEmpty()) {
            ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
            return productRepository.findByUserAndTypeAndStatus(user, ProductType.WANTED, productStatus)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        // 如果没有状态筛选，返回所有状态的求购商品
        else {
            return productRepository.findByUserAndType(user, ProductType.WANTED)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<ProductDTO> getProductsBySeller(Long userId) {
        // 直接调用获取用户商品的方法，因为功能相同
        return getProductsByUser(userId);
    }
    
    @Override
    public List<ProductDTO> getAllProducts() {
        // 获取所有商品，不分页
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    public List<ProductDTO> searchProducts(String keyword) {
        // 搜索商品，不分页，只返回活跃状态的商品
        Pageable pageable = Pageable.unpaged();
        Page<Product> products = productRepository.searchByKeyword(keyword, ProductStatus.ACTIVE, pageable);
        return products.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    public List<ProductDTO> getProductsByStatus(String status) {
        // 获取指定状态的商品
        ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
        List<Product> products = productRepository.findByStatus(productStatus);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    public List<ProductDTO> getProductsByCategory(String category) {
        // 获取指定分类的商品，只返回活跃状态的商品
        Pageable pageable = Pageable.unpaged();
        Page<Product> products = productRepository.findByCategoryAndStatus(category, ProductStatus.ACTIVE, pageable);
        return products.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
    }
} 