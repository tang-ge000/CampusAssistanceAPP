package com.campus.assistance.service;

import com.campus.assistance.dto.AuthResponse;
import com.campus.assistance.dto.LoginRequest;
import com.campus.assistance.dto.RegisterRequest;
import com.campus.assistance.dto.UserDto;
import com.campus.assistance.model.User;
import com.campus.assistance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        log.info("开始注册用户: {}", request.getStudentId());
        try {
            // 检查学号是否已存在
            if (userRepository.existsByStudentId(request.getStudentId())) {
                log.warn("注册失败: 学号 {} 已被注册", request.getStudentId());
                throw new RuntimeException("该学号已被注册");
            }

            // 检查邮箱是否已存在
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("注册失败: 邮箱 {} 已被注册", request.getEmail());
                throw new RuntimeException("该邮箱已被注册");
            }

            // 创建新用户
            User user = new User();
            user.setStudentId(request.getStudentId());
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            // 保存用户
            userRepository.save(user);
            log.info("用户注册成功: {}", request.getStudentId());

            // 生成token
            String token = jwtService.generateToken(user);

            // 返回响应
            return new AuthResponse(token, convertToDto(user));
        } catch (Exception e) {
            log.error("注册过程发生错误: ", e);
            throw e;
        }
    }

    public AuthResponse login(LoginRequest request) {
        log.info("开始登录: {}", request.getStudentId());
        try {
            // 检查用户是否存在
            User user = userRepository.findByStudentId(request.getStudentId())
                .orElseThrow(() -> {
                    log.warn("登录失败: 用户 {} 不存在", request.getStudentId());
                    return new RuntimeException("用户不存在");
                });

            // 验证密码
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("登录失败: 用户 {} 密码错误", request.getStudentId());
                throw new RuntimeException("密码错误");
            }

            // 生成token
            String token = jwtService.generateToken(user);
            log.info("用户登录成功: {}", request.getStudentId());

            // 返回响应
            return new AuthResponse(token, convertToDto(user));
        } catch (Exception e) {
            log.error("登录过程发生错误: ", e);
            throw e;
        }
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setStudentId(user.getStudentId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }
} 