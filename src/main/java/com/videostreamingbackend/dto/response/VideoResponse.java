package com.videostreamingbackend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class VideoResponse {
    private Long id;
    private String title;
    private String description;
    private Long uploaderId;
    private String uploaderName;
    private String streamUrl;
    private String thumbnailUrl;
    private Instant createdAt;
}
