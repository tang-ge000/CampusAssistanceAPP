package com.campus.assistance.repository;

import com.campus.assistance.model.Draft;
import com.campus.assistance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DraftRepository extends JpaRepository<Draft, Long> {
    List<Draft> findByUserOrderByUpdatedAtDesc(User user);
} 