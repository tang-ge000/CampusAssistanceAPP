package com.campus.assistance.repository;

import com.campus.assistance.model.Message;
import com.campus.assistance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByReceiverOrderByCreatedAtDesc(User receiver);
    Long countByReceiverAndIsRead(User receiver, Boolean isRead);
} 