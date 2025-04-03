package com.campus.assistance.repository;

import com.campus.assistance.model.Favorite;
import com.campus.assistance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserOrderByCreatedAtDesc(User user);
    Optional<Favorite> findByUserAndItemTypeAndItemId(User user, String itemType, Long itemId);
} 