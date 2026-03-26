package com.videostreamingbackend.service.impl;

import com.videostreamingbackend.dto.request.LoginRequest;
import com.videostreamingbackend.dto.request.RefreshTokenRequest;
import com.videostreamingbackend.dto.request.RegisterRequest;
import com.videostreamingbackend.dto.response.AuthResponse;
import com.videostreamingbackend.entity.Role;
import com.videostreamingbackend.entity.User;
import com.videostreamingbackend.exception.ConflictException;
import com.videostreamingbackend.repository.UserRepository;
import com.videostreamingbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuthenticationManager authenticationManager;


    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        if(userRepository.existByEmail(registerRequest.getEmail())) {
            throw new ConflictException("Email already exists: " + registerRequest.getEmail());
        }

        if(userRepository.existByUsername(registerRequest.getUsername())) {
            throw new ConflictException("Username already exists: " + registerRequest.getUsername());
        }

        User user = User.builder().
                    username(registerRequest.getUsername()).
                    email(registerRequest.getEmail()).
                    password(passwordEncoder.encode(registerRequest.getPassword())).
                    role(Role.VIEWER).
                    build();
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        try{
            //authenticationManager.authenticate(new User)
        }
        return null;
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        return null;
    }

    @Override
    public void logout(String userEmail) {

    }
}
