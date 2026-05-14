package com.vipinsharma.interviewprep.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.vipinsharma.interviewprep.model.Score;
import com.vipinsharma.interviewprep.model.Session;

@DataJpaTest
class ScoreRepositoryTest {

    @Autowired SessionRepository sessionRepository;
    @Autowired ScoreRepository scoreRepository;

    @Test
    void findBySessionId_afterSave_returnsScoreWithCorrectValue() {
        Session session = new Session();
        session.setId(UUID.randomUUID());
        session.setCompanyName("Google");
        session.setJobTitle("Staff SWE");
        session.setCompanyBrief("{}");
        session.setStatus("ACTIVE");
        sessionRepository.save(session);

        Score score = new Score();
        score.setId(UUID.randomUUID());
        score.setSession(session);
        score.setOverallScore(85);
        score.setFeedback("Good performance overall");
        scoreRepository.save(score);

        Optional<Score> found = scoreRepository.findBySessionId(session.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getOverallScore()).isEqualTo(85);
    }
}
