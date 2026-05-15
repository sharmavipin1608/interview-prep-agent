package com.vipinsharma.interviewprep.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AiConfigTest {

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Test
    void chatMemory_isNotNull() {
        assertThat(chatMemory).isNotNull();
    }

    @Test
    void chatClientBuilder_isNotNull() {
        assertThat(chatClientBuilder).isNotNull();
    }
}
