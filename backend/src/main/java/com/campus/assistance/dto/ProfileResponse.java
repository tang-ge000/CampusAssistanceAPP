package com.campus.assistance.dto;

import lombok.Data;

@Data
public class ProfileResponse {
    private Long id;
    private String studentId;
    private String username;
    private String email;
    private String avatar;
    private String phone;
    private String address;
    private String bio;
    private Long unreadMessageCount;
} 