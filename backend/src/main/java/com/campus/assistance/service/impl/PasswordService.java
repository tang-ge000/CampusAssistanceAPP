package com.campus.assistance.service.impl;

import com.campus.assistance.dto.PasswordChangeRequest;
import com.campus.assistance.dto.PasswordVerifyRequest;
import com.campus.assistance.model.User;
import com.campus.assistance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 密码管理服务
 */
@Service
public class PasswordService {

    private static final Logger log = LoggerFactory.getLogger(PasswordService.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 验证用户当前密码是否正确
     */
    public Map<String, Boolean> verifyPassword(PasswordVerifyRequest request) {
        log.info("开始验证密码: {}", request.getStudentId());
        Map<String, Boolean> result = new HashMap<>();
        
        try {
            // 获取用户
            Optional<User> userOpt = userRepository.findByStudentId(request.getStudentId());
            if (!userOpt.isPresent()) {
                log.warn("密码验证失败: 用户 {} 不存在", request.getStudentId());
                result.put("verified", false);
                return result;
            }
            
            User user = userOpt.get();
            
            // 验证密码
            boolean isValid = passwordEncoder.matches(request.getPassword(), user.getPassword());
            
            if (isValid) {
                log.info("密码验证成功: {}", request.getStudentId());
            } else {
                log.warn("密码验证失败: 用户 {} 密码不匹配", request.getStudentId());
            }
            
            result.put("verified", isValid);
            return result;
        } catch (Exception e) {
            log.error("密码验证过程发生错误: ", e);
            result.put("verified", false);
            return result;
        }
    }
    
    /**
     * 修改用户密码
     */
    public Map<String, Object> changePassword(PasswordChangeRequest request) {
        log.info("开始修改密码: {}", request.getStudentId());
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取用户
            Optional<User> userOpt = userRepository.findByStudentId(request.getStudentId());
            if (!userOpt.isPresent()) {
                log.warn("密码修改失败: 用户 {} 不存在", request.getStudentId());
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }
            
            User user = userOpt.get();
            
            // 验证当前密码
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                log.warn("密码修改失败: 用户 {} 当前密码不正确", request.getStudentId());
                result.put("success", false);
                result.put("message", "当前密码不正确");
                return result;
            }
            
            // 加密并更新新密码
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            
            log.info("密码修改成功: {}", request.getStudentId());
            result.put("success", true);
            result.put("message", "密码修改成功");
            return result;
        } catch (Exception e) {
            log.error("密码修改过程发生错误: ", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
} 