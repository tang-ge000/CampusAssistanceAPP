package com.campus.assistance.controller;

import com.campus.assistance.dto.ProfileResponse;
import com.campus.assistance.model.User;
import com.campus.assistance.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:8000")
public class ProfileController {
    @Autowired
    private ProfileService profileService;

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User user) {
        try {
            ProfileResponse profile = profileService.getProfile(user);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("获取个人资料失败: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal User user, @RequestBody Map<String, Object> profileData) {
        try {
            ProfileResponse profile = profileService.updateProfile(user, profileData);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("更新个人资料失败: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(profileService.getMessages(user));
        } catch (Exception e) {
            log.error("获取消息失败: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/history")
    public ResponseEntity<?> getBrowseHistory(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(profileService.getBrowseHistory(user));
        } catch (Exception e) {
            log.error("获取浏览历史失败: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/drafts")
    public ResponseEntity<?> getDrafts(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(profileService.getDrafts(user));
        } catch (Exception e) {
            log.error("获取草稿箱失败: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(profileService.getFavorites(user));
        } catch (Exception e) {
            log.error("获取收藏夹失败: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/purchased")
    public ResponseEntity<?> getPurchasedItems(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(profileService.getPurchasedItems(user));
        } catch (Exception e) {
            log.error("获取已购买商品失败: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/wanted")
    public ResponseEntity<?> getWantedItems(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(profileService.getWantedItems(user));
        } catch (Exception e) {
            log.error("获取求购信息失败: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/published")
    public ResponseEntity<?> getPublishedItems(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(profileService.getPublishedItems(user));
        } catch (Exception e) {
            log.error("获取发布商品失败: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/sold")
    public ResponseEntity<?> getSoldItems(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(profileService.getSoldItems(user));
        } catch (Exception e) {
            log.error("获取已售商品失败: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 