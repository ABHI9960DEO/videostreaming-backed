package com.videostreamingbackend.controller;

import com.videostreamingbackend.dto.response.ApiResponse;
import com.videostreamingbackend.dto.response.UserChannelResponse;
import com.videostreamingbackend.entity.User;
import com.videostreamingbackend.exception.ResourceNotFoundException;
import com.videostreamingbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{id}/channel")
    public ResponseEntity<ApiResponse<UserChannelResponse>> getChannel(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        UserChannelResponse response = UserChannelResponse.builder()
                .id(user.getId())
                .username(user.getDisplayUsername())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
