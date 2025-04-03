package com.campus.assistance.repository;

import com.campus.assistance.entity.Product;
import com.campus.assistance.model.User;
import com.campus.assistance.enums.ProductStatus;
import com.campus.assistance.enums.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 查找指定状态的商品
     */
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    /**
     * 查找指定状态的商品（不分页版本）
     */
    List<Product> findByStatus(ProductStatus status);

    /**
     * 查找指定分类和状态的商品
     */
    Page<Product> findByCategoryAndStatus(String category, ProductStatus status, Pageable pageable);

    /**
     * 查找指定用户发布的商品
     */
    List<Product> findByUser(User user);

    /**
     * 查找指定用户和状态的商品
     */
    List<Product> findByUserAndStatus(User user, ProductStatus status);

    /**
     * 根据关键词搜索商品标题或描述
     */
    @Query("SELECT p FROM Product p WHERE p.status = :status AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, @Param("status") ProductStatus status, Pageable pageable);

    /**
     * 根据条件筛选商品
     */
    Page<Product> findByCategoryAndConditionLevelAndStatusAndPriceBetween(
            String category, 
            String conditionLevel, 
            ProductStatus status, 
            Double minPrice, 
            Double maxPrice, 
            Pageable pageable
    );
    
    /**
     * 查找指定类型和状态的商品
     */
    Page<Product> findByTypeAndStatus(ProductType type, ProductStatus status, Pageable pageable);
    
    /**
     * 查找指定类型、分类和状态的商品
     */
    Page<Product> findByTypeAndCategoryAndStatus(ProductType type, String category, ProductStatus status, Pageable pageable);
    
    /**
     * 查找指定类型、紧急程度和状态的商品
     */
    Page<Product> findByTypeAndUrgencyLevelAndStatus(ProductType type, String urgencyLevel, ProductStatus status, Pageable pageable);
    
    /**
     * 查找指定类型、分类、紧急程度和状态的商品
     */
    Page<Product> findByTypeAndCategoryAndUrgencyLevelAndStatus(
            ProductType type, 
            String category, 
            String urgencyLevel, 
            ProductStatus status, 
            Pageable pageable
    );
    
    /**
     * 根据关键词搜索求购商品标题或描述
     */
    @Query("SELECT p FROM Product p WHERE p.type = com.campus.assistance.enums.ProductType.WANTED AND p.status = com.campus.assistance.enums.ProductStatus.ACTIVE AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchWantedProductsByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 查找指定用户和类型的商品
     */
    List<Product> findByUserAndType(User user, ProductType type);
    
    /**
     * 查找指定用户、类型和状态的商品
     */
    List<Product> findByUserAndTypeAndStatus(User user, ProductType type, ProductStatus status);
    
    /**
     * 查找指定用户发布的商品并按发布时间倒序排序
     */
    List<Product> findByUserOrderByPublishTimeDesc(User user);
} 