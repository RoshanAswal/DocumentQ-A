package com.project.ragqa.dto;

import com.project.ragqa.entity.User;

import java.util.UUID;

public record UserResponse(UUID id, String email, String role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole().name());
    }
}