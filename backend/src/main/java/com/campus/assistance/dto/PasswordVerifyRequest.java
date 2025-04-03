package com.campus.assistance.dto;

import lombok.Data;

@Data
public class PasswordVerifyRequest {
    private String studentId;
    private String password;
} 