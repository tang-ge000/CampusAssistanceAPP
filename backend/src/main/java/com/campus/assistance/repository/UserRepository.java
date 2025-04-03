package com.campus.assistance.repository;

import com.campus.assistance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentId(String studentId);
    Optional<User> findByEmail(String email);
    boolean existsByStudentId(String studentId);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
} 