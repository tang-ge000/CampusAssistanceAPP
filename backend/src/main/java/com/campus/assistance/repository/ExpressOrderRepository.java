package com.campus.assistance.repository;

import com.campus.assistance.model.ExpressOrder;
import com.campus.assistance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpressOrderRepository extends JpaRepository<ExpressOrder, Long> {
    List<ExpressOrder> findByIssuerOrderByCreatedAtDesc(User issuer);
    List<ExpressOrder> findByTakerOrderByCreatedAtDesc(User taker);
    List<ExpressOrder> findByStatusOrderByCreatedAtDesc(String status);
} 