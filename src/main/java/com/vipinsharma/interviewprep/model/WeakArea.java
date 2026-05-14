package com.vipinsharma.interviewprep.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "weak_areas")
public class WeakArea {

    @Id
    private UUID id;

    @Column(name = "topic")
    private String topic;

    @Column(name = "frequency")
    private Integer frequency;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (frequency == null) frequency = 1;
        if (lastSeen == null) lastSeen = LocalDateTime.now();
    }

    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public String getTopic() { return topic; }

    public void setTopic(String topic) { this.topic = topic; }

    public Integer getFrequency() { return frequency; }

    public void setFrequency(Integer frequency) { this.frequency = frequency; }

    public LocalDateTime getLastSeen() { return lastSeen; }

    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
}
