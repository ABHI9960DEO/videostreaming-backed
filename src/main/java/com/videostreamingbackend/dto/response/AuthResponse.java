package com.videostreamingbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserSummary userSummary;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String username;
        private String email;
        private String role;
        private String avatarUrl;
    }
}
