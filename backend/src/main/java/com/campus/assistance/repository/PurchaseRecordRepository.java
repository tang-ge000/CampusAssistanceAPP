package com.campus.assistance.repository;

import com.campus.assistance.model.PurchaseRecord;
import com.campus.assistance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRecordRepository extends JpaRepository<PurchaseRecord, Long> {
    List<PurchaseRecord> findByBuyerOrderByPurchaseTimeDesc(User buyer);
    List<PurchaseRecord> findBySellerOrderByPurchaseTimeDesc(User seller);
} 