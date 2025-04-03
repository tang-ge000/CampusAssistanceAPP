package com.campus.assistance.service;

import com.campus.assistance.dto.ProductDTO;
import com.campus.assistance.entity.Product;
import com.campus.assistance.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    
    /**
     * 创建新商品
     * @param productDTO 商品DTO数据
     * @param userId 用户ID
     * @return 创建的商品DTO
     */
    ProductDTO createProduct(ProductDTO productDTO, Long userId);
    
    /**
     * 获取所有商品列表
     * @return 商品列表
     */
    List<ProductDTO> getAllProducts();
    
    /**
     * 按分类获取商品列表
     * @param category 分类
     * @return 该分类下的商品列表
     */
    List<ProductDTO> getProductsByCategory(String category);
    
    /**
     * 按状态获取商品列表
     * @param status 状态
     * @return 该状态下的商品列表
     */
    List<ProductDTO> getProductsByStatus(String status);
    
    /**
     * 获取用户发布的商品列表
     * @param userId 用户ID
     * @return 该用户发布的商品列表
     */
    List<ProductDTO> getProductsBySeller(Long userId);
    
    /**
     * 获取商品详情
     * @param productId 商品ID
     * @return 商品详情DTO
     */
    ProductDTO getProductById(Long productId);
    
    /**
     * 更新商品信息
     * @param productId 商品ID
     * @param productDTO 商品DTO数据
     * @param userId 用户ID（需验证权限）
     * @return 更新后的商品DTO
     */
    ProductDTO updateProduct(Long productId, ProductDTO productDTO, Long userId);
    
    /**
     * 修改商品状态
     * @param productId 商品ID
     * @param status 新状态
     * @param userId 用户ID（需验证权限）
     * @return 更新后的商品DTO
     */
    ProductDTO updateProductStatus(Long productId, ProductStatus status, Long userId);
    
    /**
     * 删除商品
     * @param productId 商品ID
     * @param userId 用户ID（需验证权限）
     */
    void deleteProduct(Long productId, Long userId);
    
    /**
     * 搜索商品
     * @param keyword 关键词
     * @return 匹配的商品列表
     */
    List<ProductDTO> searchProducts(String keyword);
    
    /**
     * 将Product转换为ProductDTO
     * @param product 商品实体
     * @return 商品DTO
     */
    ProductDTO convertToDTO(Product product);

    /**
     * 获取所有在售商品
     */
    Page<ProductDTO> getAllActiveProducts(Pageable pageable);

    /**
     * 获取指定分类的在售商品
     */
    Page<ProductDTO> getProductsByCategory(String category, Pageable pageable);

    /**
     * 搜索商品
     */
    Page<ProductDTO> searchProducts(String keyword, Pageable pageable);

    /**
     * 根据条件筛选商品
     */
    Page<ProductDTO> filterProducts(String category, String condition, Double minPrice, Double maxPrice, Pageable pageable);

    /**
     * 获取指定用户的所有商品
     */
    List<ProductDTO> getProductsByUser(Long userId);

    /**
     * 获取指定用户和状态的商品
     */
    List<ProductDTO> getProductsByUserAndStatus(Long userId, ProductStatus status);

    /**
     * 增加商品浏览次数
     */
    void incrementViewCount(Long id);
    
    /**
     * 获取所有求购商品（支持按分类和紧急程度筛选）
     */
    Page<ProductDTO> getWantedProducts(String category, String urgency, Pageable pageable);
    
    /**
     * 搜索求购商品
     */
    Page<ProductDTO> searchWantedProducts(String keyword, Pageable pageable);
    
    /**
     * 获取用户的求购商品
     */
    List<ProductDTO> getUserWantedProducts(Long userId, String status);
} 