package com.campus.assistance.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String studentId;
    private String username;
    private String email;
    private String password;
} 