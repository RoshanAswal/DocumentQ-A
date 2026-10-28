package com.project.ragqa.controller;

import com.project.ragqa.dto.UserResponse;
import com.project.ragqa.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    @GetMapping("/api/me")
    public UserResponse me(@AuthenticationPrincipal User user) {
        return UserResponse.from(user);
    }
}