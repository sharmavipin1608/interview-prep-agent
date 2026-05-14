package com.vipinsharma.interviewprep.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vipinsharma.interviewprep.model.WeakArea;

public interface WeakAreaRepository extends JpaRepository<WeakArea, UUID> {
    Optional<WeakArea> findByTopic(String topic);

    List<WeakArea> findTop5ByOrderByFrequencyDesc();
}
