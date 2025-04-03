package com.campus.assistance.repository;

import com.campus.assistance.model.BrowseHistory;
import com.campus.assistance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrowseHistoryRepository extends JpaRepository<BrowseHistory, Long> {
    List<BrowseHistory> findByUserOrderByBrowseTimeDesc(User user);
} 