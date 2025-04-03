package com.campus.assistance.repository;

import com.campus.assistance.model.User;
import com.campus.assistance.model.WantedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WantedItemRepository extends JpaRepository<WantedItem, Long> {
    List<WantedItem> findByUserOrderByCreatedAtDesc(User user);
    List<WantedItem> findByStatusOrderByCreatedAtDesc(String status);
} 