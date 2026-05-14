package com.vipinsharma.interviewprep.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.vipinsharma.interviewprep.model.Message;
import com.vipinsharma.interviewprep.model.Session;

@DataJpaTest
class SessionRepositoryTest {

    @Autowired SessionRepository sessionRepository;
    @Autowired MessageRepository messageRepository;

    @Test
    void findById_afterSave_returnsPersistedSession() {
        Session session = new Session();
        session.setId(UUID.randomUUID());
        session.setCompanyName("Stripe");
        session.setJobTitle("Senior SWE");
        session.setCompanyBrief("{\"summary\":\"Payments company\"}");
        session.setStatus("ACTIVE");
        sessionRepository.save(session);

        Session found = sessionRepository.findById(session.getId()).orElseThrow();
        assertThat(found.getCompanyName()).isEqualTo("Stripe");
    }

    @Test
    void findBySessionId_afterSave_returnsMessagesInOrder_hasSize() {
        Session session = new Session();
        session.setId(UUID.randomUUID());
        session.setCompanyName("Stripe");
        session.setJobTitle("Senior SWE");
        session.setCompanyBrief("{}");
        session.setStatus("ACTIVE");
        sessionRepository.save(session);

        Message msg = new Message();
        msg.setId(UUID.randomUUID());
        msg.setSession(session);
        msg.setRole("USER");
        msg.setContent("Tell me about yourself");
        messageRepository.save(msg);

        List<Message> messages =
                messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        assertThat(messages).hasSize(1);
    }

    @Test
    void findBySessionId_afterSave_returnsMessagesInOrder_hasCorrectContent() {
        Session session = new Session();
        session.setId(UUID.randomUUID());
        session.setCompanyName("Stripe");
        session.setJobTitle("Senior SWE");
        session.setCompanyBrief("{}");
        session.setStatus("ACTIVE");
        sessionRepository.save(session);

        Message msg = new Message();
        msg.setId(UUID.randomUUID());
        msg.setSession(session);
        msg.setRole("USER");
        msg.setContent("Tell me about yourself");
        messageRepository.save(msg);

        List<Message> messages =
                messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        assertThat(messages.get(0).getContent()).isEqualTo("Tell me about yourself");
    }

    @Test
    void prePersist_withNoFieldsSet_populatesIdCreatedAtAndStatus() {
        Session session = new Session();
        session.setCompanyName("Meta");
        session.setJobTitle("SWE II");
        sessionRepository.save(session);

        Session found = sessionRepository.findById(session.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getStatus()).isEqualTo("ACTIVE");
    }
}
