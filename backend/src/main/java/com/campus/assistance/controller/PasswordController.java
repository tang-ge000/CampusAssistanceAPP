package com.campus.assistance.controller;

import com.campus.assistance.dto.PasswordChangeRequest;
import com.campus.assistance.dto.PasswordVerifyRequest;
import com.campus.assistance.service.impl.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8000")
public class PasswordController {

    private static final Logger log = LoggerFactory.getLogger(PasswordController.class);

    @Autowired
    private PasswordService passwordService;

    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody PasswordVerifyRequest request) {
        try {
            log.info("尝试验证密码: {}", request.getStudentId());
            Map<String, Boolean> result = passwordService.verifyPassword(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("密码验证失败: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request) {
        try {
            log.info("尝试修改密码: {}", request.getStudentId());
            Map<String, Object> result = passwordService.changePassword(request);
            
            if ((Boolean) result.get("success")) {
                log.info("密码修改成功: {}", request.getStudentId());
                return ResponseEntity.ok(result);
            } else {
                log.warn("密码修改失败: {}", result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("密码修改失败: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 