package com.videostreamingbackend.repository;

import com.videostreamingbackend.entity.RefreshToken;
import com.videostreamingbackend.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String refreshToken);

    @Modifying
    @Query("UPDATE RefreshToken  rt SET rt.revoked = true where rt.user = : user")
    void revokeAllByUser(User user);
}
