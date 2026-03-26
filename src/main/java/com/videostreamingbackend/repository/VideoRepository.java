package com.videostreamingbackend.repository;

import com.videostreamingbackend.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {
}
