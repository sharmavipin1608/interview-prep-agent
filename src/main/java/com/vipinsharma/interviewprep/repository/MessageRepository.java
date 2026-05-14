package com.vipinsharma.interviewprep.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vipinsharma.interviewprep.model.Message;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
}
