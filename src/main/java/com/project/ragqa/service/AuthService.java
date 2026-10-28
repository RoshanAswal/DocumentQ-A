package com.project.ragqa.service;

import com.project.ragqa.dto.AuthResponse;
import com.project.ragqa.dto.LoginRequest;
import com.project.ragqa.dto.RegisterRequest;
import com.project.ragqa.dto.UserResponse;
import com.project.ragqa.entity.User;
import com.project.ragqa.exception.EmailAlreadyExistsException;
import com.project.ragqa.exception.InvalidCredentialsException;
import com.project.ragqa.repository.UserRepository;
import com.project.ragqa.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest request) {
        String email = normalize(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        User user = new User(email, passwordEncoder.encode(request.password()));
        try {
            return UserResponse.from(userRepository.save(user));
        } catch (DataIntegrityViolationException ex) {
            // two registrations raced past the check above; the UNIQUE constraint caught it
            throw new EmailAlreadyExistsException("Email is already registered");
        }
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalize(request.email()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return AuthResponse.bearer(jwtService.generateToken(user));
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}