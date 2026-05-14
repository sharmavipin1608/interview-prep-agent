package com.vipinsharma.interviewprep.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vipinsharma.interviewprep.model.Session;

public interface SessionRepository extends JpaRepository<Session, UUID> {}
