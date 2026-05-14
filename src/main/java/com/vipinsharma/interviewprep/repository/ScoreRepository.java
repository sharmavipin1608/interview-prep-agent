package com.vipinsharma.interviewprep.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vipinsharma.interviewprep.model.Score;

public interface ScoreRepository extends JpaRepository<Score, UUID> {
    Optional<Score> findBySessionId(UUID sessionId);
}
