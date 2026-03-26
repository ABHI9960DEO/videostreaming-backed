package com.videostreamingbackend.service;


import com.videostreamingbackend.dto.request.LoginRequest;
import com.videostreamingbackend.dto.request.RefreshTokenRequest;
import com.videostreamingbackend.dto.request.RegisterRequest;
import com.videostreamingbackend.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(String userEmail);
}
