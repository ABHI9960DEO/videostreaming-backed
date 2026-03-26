package com.videostreamingbackend.controller;

import com.videostreamingbackend.dto.response.ApiResponse;
import com.videostreamingbackend.dto.response.VideoResponse;
import com.videostreamingbackend.entity.User;
import com.videostreamingbackend.entity.Video;
import com.videostreamingbackend.exception.BadRequestException;
import com.videostreamingbackend.exception.ResourceNotFoundException;
import com.videostreamingbackend.repository.UserRepository;
import com.videostreamingbackend.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final com.videostreamingbackend.config.AppProperties appProperties;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VideoResponse>>> listVideos() {
        List<VideoResponse> videos = videoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(videos));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<VideoResponse>> uploadVideo(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new BadRequestException("Authenticated user is required");
        }
        if (file.isEmpty()) {
            throw new BadRequestException("Video file is required");
        }
        if (!StringUtils.hasText(title)) {
            throw new BadRequestException("Title is required");
        }

        User uploader = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for current session"));

        Path basePath = resolveAndCreateBasePath();
        String originalName = file.getOriginalFilename() == null ? "video.bin" : file.getOriginalFilename();
        String storedName = UUID.randomUUID() + "-" + originalName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        Path destination = basePath.resolve(storedName).normalize();

        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        Video saved = videoRepository.save(Video.builder()
                .title(title)
                .description(description)
                .uploader(uploader)
                .storagePath(destination.toString())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .createdAt(Instant.now())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toResponse(saved)));
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamVideo(@PathVariable Long id, @RequestHeader(value = "Range", required = false) String rangeHeader)
            throws IOException {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video", id));

        Path path = Paths.get(video.getStoragePath());
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Video file not found");
        }

        long fileLength = Files.size(path);
        String contentType = StringUtils.hasText(video.getContentType()) ? video.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        if (rangeHeader == null || !rangeHeader.startsWith("bytes=")) {
            Resource resource = toResource(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(fileLength)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(resource);
        }

        long start = 0;
        long end = fileLength - 1;
        String[] ranges = rangeHeader.substring("bytes=".length()).split("-", 2);
        if (!ranges[0].isBlank()) {
            start = Long.parseLong(ranges[0]);
        }
        if (ranges.length > 1 && !ranges[1].isBlank()) {
            end = Long.parseLong(ranges[1]);
        }
        if (start > end || end >= fileLength) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileLength)
                    .build();
        }

        byte[] data = Files.readAllBytes(path);
        int len = (int) (end - start + 1);
        byte[] chunk = new byte[len];
        System.arraycopy(data, (int) start, chunk, 0, len);
        Resource resource = new org.springframework.core.io.ByteArrayResource(chunk);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(len))
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                .body(resource);
    }

    private Path resolveAndCreateBasePath() throws IOException {
        String configured = appProperties.getStorage().getLocal().getBasePath();
        Path basePath;
        if (!StringUtils.hasText(configured)) {
            basePath = Paths.get(System.getProperty("user.home"), "videostream", "uploads");
        } else {
            basePath = Paths.get(configured);
        }
        Files.createDirectories(basePath);
        return basePath.toAbsolutePath().normalize();
    }

    private Resource toResource(Path path) {
        try {
            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new BadRequestException("Invalid video path");
        }
    }

    private VideoResponse toResponse(Video video) {
        return VideoResponse.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .uploaderId(video.getUploader().getId())
                .uploaderName(video.getUploader().getDisplayUsername())
                .streamUrl("/api/videos/" + video.getId() + "/stream")
                .thumbnailUrl(null)
                .createdAt(video.getCreatedAt())
                .build();
    }
}
