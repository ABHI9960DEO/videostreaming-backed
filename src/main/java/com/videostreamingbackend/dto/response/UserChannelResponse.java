package com.videostreamingbackend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserChannelResponse {
    private Long id;
    private String username;
    private String bio;
    private String avatarUrl;
}
