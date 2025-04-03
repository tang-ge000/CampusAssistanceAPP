package com.campus.assistance.dto;

import lombok.Data;

@Data
public class PasswordChangeRequest {
    private String studentId;
    private String currentPassword;
    private String newPassword;
} 