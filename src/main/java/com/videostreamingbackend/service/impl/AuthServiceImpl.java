package com.videostreamingbackend.service.impl;

import com.videostreamingbackend.dto.request.LoginRequest;
import com.videostreamingbackend.dto.request.RefreshTokenRequest;
import com.videostreamingbackend.dto.request.RegisterRequest;
import com.videostreamingbackend.dto.response.AuthResponse;
import com.videostreamingbackend.config.AppProperties;
import com.videostreamingbackend.entity.Role;
import com.videostreamingbackend.entity.RefreshToken;
import com.videostreamingbackend.entity.User;
import com.videostreamingbackend.exception.BadRequestException;
import com.videostreamingbackend.exception.ConflictException;
import com.videostreamingbackend.exception.UnauthorizedException;
import com.videostreamingbackend.repository.RefreshTokenRepository;
import com.videostreamingbackend.repository.UserRepository;
import com.videostreamingbackend.security.JwtUtil;
import com.videostreamingbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AppProperties appProperties;


    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("Email already exists: " + registerRequest.getEmail());
        }

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ConflictException("Username already exists: " + registerRequest.getUsername());
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.VIEWER)
                .build();
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUser(user);
        RefreshToken refreshToken = createRefreshToken(user);
        return buildAuthResponse(user, refreshToken.getToken());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        refreshTokenRepository.revokeAllByUser(user);
        RefreshToken refreshToken = createRefreshToken(user);
        return buildAuthResponse(user, refreshToken.getToken());
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new UnauthorizedException("Refresh token revoked");
        }
        if (refreshToken.isExpired()) {
            throw new UnauthorizedException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        if (user == null) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        RefreshToken newRefreshToken = createRefreshToken(user);
        return buildAuthResponse(user, newRefreshToken.getToken());
    }

    @Override
    @Transactional
    public void logout(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestException("Unknown user: " + userEmail));

        refreshTokenRepository.revokeAllByUser(user);
    }

    private RefreshToken createRefreshToken(User user) {
        Long configuredMs = appProperties.getJwt().getRefreshTokenExpiresMs();
        if (configuredMs == null || configuredMs <= 0) {
            configuredMs = 7L * 24 * 60 * 60 * 1000;
        }

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(configuredMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(token);
    }

    private AuthResponse buildAuthResponse(User user, String refreshToken) {
        String accessToken = jwtUtil.generateAccessToken(user);
        Long accessExpiryMs = appProperties.getJwt().getAccessTokenExpiresMs();
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessExpiryMs == null ? null : (accessExpiryMs / 1000))
                .userSummary(AuthResponse.UserSummary.builder()
                        .id(user.getId())
                        .username(user.getDisplayUsername())
                        .email(user.getEmail())
                        .role(user.getRole() == null ? null : user.getRole().name())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .build();
    }
}
