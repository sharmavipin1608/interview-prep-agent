# Interview Prep Agent — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a REST API-backed Spring AI agent that researches companies, conducts mock interviews, and coaches users with scored feedback, deployed to Oracle Cloud Free Tier via GitHub Actions.

**Architecture:** Three Spring AI agents (ResearchAgent, InterviewerAgent, CoachAgent) orchestrated by five services, exposed via two REST controllers. Services pass data between agents — agents never call each other directly. H2 for dev, PostgreSQL for prod via Spring profiles.

**Tech Stack:** Java 21, Spring Boot 3.3.4, Spring AI 1.0.0, Gradle (Groovy DSL), OpenAI gpt-4o-mini, Tavily Search API, H2 (dev), PostgreSQL (prod), GitHub Actions, Oracle Cloud Ampere A1, systemd, Caddy

---

## File Map

```
interview-prep-agent/
  build.gradle
  settings.gradle
  src/
    main/
      java/com/vipinsharma/interviewprep/
        InterviewPrepAgentApplication.java
        config/
          AiConfig.java
        model/
          Session.java
          Message.java
          Score.java
          WeakArea.java
        repository/
          SessionRepository.java
          MessageRepository.java
          ScoreRepository.java
          WeakAreaRepository.java
        dto/
          ResearchRequest.java       ← Java record
          CompanyBrief.java          ← Java record
          StartSessionRequest.java   ← Java record
          ChatRequest.java           ← Java record
          ChatResponse.java          ← Java record
          SessionFeedback.java       ← Java record
          SessionSummary.java        ← Java record
        agent/
          tools/
            TavilySearchTool.java
          ResearchAgent.java
          InterviewerAgent.java
          CoachAgent.java
        service/
          ResearchService.java
          InterviewSessionService.java
          ChatService.java
          ScoringService.java
          SessionHistoryService.java
        api/
          ResearchController.java
          SessionController.java
      resources/
        application.yml
        application-dev.yml
        application-prod.yml
    test/
      java/com/vipinsharma/interviewprep/
        repository/
          SessionRepositoryTest.java
        agent/
          ResearchAgentTest.java
          InterviewerAgentTest.java
          CoachAgentTest.java
        service/
          ResearchServiceTest.java
          InterviewSessionServiceTest.java
          ChatServiceTest.java
          ScoringServiceTest.java
          SessionHistoryServiceTest.java
        api/
          ResearchControllerTest.java
          SessionControllerTest.java
  .github/
    workflows/
      deploy.yml
  deploy/
    interview-prep-agent.service
    Caddyfile
```

---

### Task 1: Gradle project setup and application bootstrap

**Files:**
- Create: `build.gradle`
- Create: `settings.gradle`
- Create: `src/main/java/com/vipinsharma/interviewprep/InterviewPrepAgentApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-dev.yml`
- Create: `src/main/resources/application-prod.yml`
- Create: `src/test/java/com/vipinsharma/interviewprep/InterviewPrepAgentApplicationTests.java`

- [ ] **Step 1: Create settings.gradle**

```groovy
rootProject.name = 'interview-prep-agent'
```

- [ ] **Step 2: Create build.gradle**

```groovy
plugins {
    id 'org.springframework.boot' version '3.3.4'
    id 'io.spring.dependency-management' version '1.1.6'
    id 'java'
}

group = 'com.vipinsharma'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

ext {
    springAiVersion = '1.0.0'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.ai:spring-ai-openai-spring-boot-starter'

    runtimeOnly 'com.h2database:h2'
    runtimeOnly 'org.postgresql:postgresql'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.ai:spring-ai-test'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.ai:spring-ai-bom:${springAiVersion}"
    }
}

tasks.named('test') {
    useJUnitPlatform()
}
```

- [ ] **Step 3: Generate Gradle wrapper**

```bash
cd ~/Projects/interview-prep-agent
gradle wrapper --gradle-version 8.10
```

Expected: `gradlew`, `gradlew.bat`, and `gradle/wrapper/` appear in the project root.

- [ ] **Step 4: Create main application class**

`src/main/java/com/vipinsharma/interviewprep/InterviewPrepAgentApplication.java`:

```java
package com.vipinsharma.interviewprep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InterviewPrepAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(InterviewPrepAgentApplication.class, args);
    }
}
```

- [ ] **Step 5: Create application.yml**

`src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: interview-prep-agent
  profiles:
    active: dev

server:
  port: 8080
```

- [ ] **Step 6: Create application-dev.yml**

`src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:interviewdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini

logging:
  level:
    com.vipinsharma: DEBUG
    org.springframework.ai: DEBUG

tavily:
  api-key: ${TAVILY_API_KEY}
```

- [ ] **Step 7: Create application-prod.yml**

`src/main/resources/application-prod.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/interviewdb
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini

logging:
  level:
    root: INFO
    com.vipinsharma: INFO

tavily:
  api-key: ${TAVILY_API_KEY}
```

- [ ] **Step 8: Write smoke test**

`src/test/java/com/vipinsharma/interviewprep/InterviewPrepAgentApplicationTests.java`:

```java
package com.vipinsharma.interviewprep;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.openai.api-key=test-key",
    "tavily.api-key=test-key"
})
class InterviewPrepAgentApplicationTests {
    @Test
    void contextLoads() {}
}
```

- [ ] **Step 9: Run the smoke test**

```bash
cd ~/Projects/interview-prep-agent
./gradlew test --tests "com.vipinsharma.interviewprep.InterviewPrepAgentApplicationTests"
```

Expected: `BUILD SUCCESSFUL` — context loads with test keys.

- [ ] **Step 10: Commit**

```bash
git add build.gradle settings.gradle gradlew gradlew.bat gradle/ src/
git commit -m "chore: bootstrap Spring Boot 3 project with Spring AI and Gradle"
```

---

### Task 2: JPA entities and repositories

**Files:**
- Create: `src/main/java/com/vipinsharma/interviewprep/model/Session.java`
- Create: `src/main/java/com/vipinsharma/interviewprep/model/Message.java`
- Create: `src/main/java/com/vipinsharma/interviewprep/model/Score.java`
- Create: `src/main/java/com/vipinsharma/interviewprep/model/WeakArea.java`
- Create: `src/main/java/com/vipinsharma/interviewprep/repository/SessionRepository.java`
- Create: `src/main/java/com/vipinsharma/interviewprep/repository/MessageRepository.java`
- Create: `src/main/java/com/vipinsharma/interviewprep/repository/ScoreRepository.java`
- Create: `src/main/java/com/vipinsharma/interviewprep/repository/WeakAreaRepository.java`
- Test: `src/test/java/com/vipinsharma/interviewprep/repository/SessionRepositoryTest.java`

- [ ] **Step 1: Write the repository test first**

`src/test/java/com/vipinsharma/interviewprep/repository/SessionRepositoryTest.java`:

```java
package com.vipinsharma.interviewprep.repository;

import com.vipinsharma.interviewprep.model.Message;
import com.vipinsharma.interviewprep.model.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SessionRepositoryTest {

    @Autowired SessionRepository sessionRepository;
    @Autowired MessageRepository messageRepository;

    @Test
    void savesSessionAndRetrievesById() {
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
    void savesMessagesLinkedToSession() {
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

        List<Message> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getContent()).isEqualTo("Tell me about yourself");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.repository.SessionRepositoryTest"
```

Expected: FAIL — `Session`, `Message`, `SessionRepository`, `MessageRepository` not found.

- [ ] **Step 3: Create Session entity**

`src/main/java/com/vipinsharma/interviewprep/model/Session.java`:

```java
package com.vipinsharma.interviewprep.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    private UUID id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "company_brief", columnDefinition = "TEXT")
    private String companyBrief;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getCompanyBrief() { return companyBrief; }
    public void setCompanyBrief(String companyBrief) { this.companyBrief = companyBrief; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

- [ ] **Step 4: Create Message entity**

`src/main/java/com/vipinsharma/interviewprep/model/Message.java`:

```java
package com.vipinsharma.interviewprep.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(name = "role")
    private String role;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

- [ ] **Step 5: Create Score entity**

`src/main/java/com/vipinsharma/interviewprep/model/Score.java`:

```java
package com.vipinsharma.interviewprep.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scores")
public class Score {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(name = "overall_score")
    private Integer overallScore;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }
    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

- [ ] **Step 6: Create WeakArea entity**

`src/main/java/com/vipinsharma/interviewprep/model/WeakArea.java`:

```java
package com.vipinsharma.interviewprep.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

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
```

- [ ] **Step 7: Create repositories**

`src/main/java/com/vipinsharma/interviewprep/repository/SessionRepository.java`:

```java
package com.vipinsharma.interviewprep.repository;

import com.vipinsharma.interviewprep.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {}
```

`src/main/java/com/vipinsharma/interviewprep/repository/MessageRepository.java`:

```java
package com.vipinsharma.interviewprep.repository;

import com.vipinsharma.interviewprep.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
}
```

`src/main/java/com/vipinsharma/interviewprep/repository/ScoreRepository.java`:

```java
package com.vipinsharma.interviewprep.repository;

import com.vipinsharma.interviewprep.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ScoreRepository extends JpaRepository<Score, UUID> {
    Optional<Score> findBySessionId(UUID sessionId);
}
```

`src/main/java/com/vipinsharma/interviewprep/repository/WeakAreaRepository.java`:

```java
package com.vipinsharma.interviewprep.repository;

import com.vipinsharma.interviewprep.model.WeakArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WeakAreaRepository extends JpaRepository<WeakArea, UUID> {
    Optional<WeakArea> findByTopic(String topic);

    @Query("SELECT w FROM WeakArea w ORDER BY w.frequency DESC LIMIT 5")
    List<WeakArea> findTop5ByFrequency();
}
```

- [ ] **Step 8: Run tests to verify they pass**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.repository.SessionRepositoryTest"
```

Expected: `BUILD SUCCESSFUL` — both tests pass.

- [ ] **Step 9: Commit**

```bash
git add src/
git commit -m "feat: add JPA entities and repositories"
```

---

### Task 3: DTOs as Java records

**Files:**
- Create: `src/main/java/com/vipinsharma/interviewprep/dto/ResearchRequest.java`
- Create: `src/main/java/com/vipinsharma/interviewprep/dto/CompanyBrief.java`
- Create: `src/main/java/com/vipinsharma/interviewprep/dto/StartSessionRequest.java`
- Create: `src/main/java/com/vipinsharma/interviewprep/dto/ChatRequest.java`
- Create: `src/main/java/com/vipinsharma/interviewprep/dto/ChatResponse.java`
- Create: `src/main/java/com/vipinsharma/interviewprep/dto/SessionFeedback.java`
- Create: `src/main/java/com/vipinsharma/interviewprep/dto/SessionSummary.java`

- [ ] **Step 1: Create all DTO records**

`src/main/java/com/vipinsharma/interviewprep/dto/ResearchRequest.java`:
```java
package com.vipinsharma.interviewprep.dto;

public record ResearchRequest(
    String companyName,
    String jobTitle,
    String jobDescription
) {}
```

`src/main/java/com/vipinsharma/interviewprep/dto/CompanyBrief.java`:
```java
package com.vipinsharma.interviewprep.dto;

import java.util.List;

public record CompanyBrief(
    String companyName,
    String companySummary,
    List<String> techStack,
    String cultureSignals,
    String recentNews,
    List<String> likelyInterviewTopics
) {}
```

`src/main/java/com/vipinsharma/interviewprep/dto/StartSessionRequest.java`:
```java
package com.vipinsharma.interviewprep.dto;

import java.util.UUID;

public record StartSessionRequest(
    UUID companyBriefSessionId,
    String jobDescription
) {}
```

`src/main/java/com/vipinsharma/interviewprep/dto/ChatRequest.java`:
```java
package com.vipinsharma.interviewprep.dto;

public record ChatRequest(String message) {}
```

`src/main/java/com/vipinsharma/interviewprep/dto/ChatResponse.java`:
```java
package com.vipinsharma.interviewprep.dto;

public record ChatResponse(String reply) {}
```

`src/main/java/com/vipinsharma/interviewprep/dto/SessionFeedback.java`:
```java
package com.vipinsharma.interviewprep.dto;

import java.util.List;
import java.util.UUID;

public record SessionFeedback(
    UUID sessionId,
    int overallScore,
    List<String> weakAreas,
    List<String> improvementSuggestions,
    String detailedFeedback
) {}
```

`src/main/java/com/vipinsharma/interviewprep/dto/SessionSummary.java`:
```java
package com.vipinsharma.interviewprep.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionSummary(
    UUID id,
    String companyName,
    String jobTitle,
    String status,
    Integer overallScore,
    LocalDateTime createdAt
) {}
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL` — all records compile cleanly.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/vipinsharma/interviewprep/dto/
git commit -m "feat: add DTOs as Java records"
```

---

### Task 4: TavilySearchTool

**Files:**
- Create: `src/main/java/com/vipinsharma/interviewprep/agent/tools/TavilySearchTool.java`
- Test: `src/test/java/com/vipinsharma/interviewprep/agent/TavilySearchToolTest.java`

- [ ] **Step 1: Write the failing test**

`src/test/java/com/vipinsharma/interviewprep/agent/TavilySearchToolTest.java`:

```java
package com.vipinsharma.interviewprep.agent;

import com.vipinsharma.interviewprep.agent.tools.TavilySearchTool;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TavilySearchToolTest {

    @Test
    void search_returnsFormattedResults() {
        String fakeResponse = """
            {"results":[
              {"title":"Stripe Engineering","url":"https://stripe.com/blog","content":"Stripe uses Java, Ruby, Go"},
              {"title":"Stripe Culture","url":"https://stripe.com/jobs","content":"Focus on impact and ownership"}
            ]}
            """;

        RestClient.Builder builder = mock(RestClient.Builder.class, RETURNS_DEEP_STUBS);
        when(builder
            .baseUrl(anyString()).build()
            .post().uri(anyString())
            .header(any(), any())
            .body(any())
            .retrieve()
            .body(String.class))
            .thenReturn(fakeResponse);

        TavilySearchTool tool = new TavilySearchTool(builder, "test-api-key");
        String result = tool.search("Stripe engineering culture");

        assertThat(result).contains("Stripe Engineering");
        assertThat(result).contains("Java, Ruby, Go");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.agent.TavilySearchToolTest"
```

Expected: FAIL — `TavilySearchTool` not found.

- [ ] **Step 3: Implement TavilySearchTool**

`src/main/java/com/vipinsharma/interviewprep/agent/tools/TavilySearchTool.java`:

```java
package com.vipinsharma.interviewprep.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class TavilySearchTool {

    private final RestClient restClient;
    private final String apiKey;

    public TavilySearchTool(
            RestClient.Builder restClientBuilder,
            @Value("${tavily.api-key}") String apiKey) {
        this.restClient = restClientBuilder.baseUrl("https://api.tavily.com").build();
        this.apiKey = apiKey;
    }

    @Tool(description = "Search the web for information about companies, technologies, engineering culture, and job interview topics")
    public String search(String query) {
        Map<String, Object> requestBody = Map.of(
            "api_key", apiKey,
            "query", query,
            "search_depth", "basic",
            "max_results", 5
        );

        String response = restClient.post()
            .uri("/search")
            .header("Content-Type", "application/json")
            .body(requestBody)
            .retrieve()
            .body(String.class);

        return response != null ? response : "No results found for: " + query;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.agent.TavilySearchToolTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add src/
git commit -m "feat: add TavilySearchTool with @Tool annotation"
```

---

### Task 5: AI configuration

**Files:**
- Create: `src/main/java/com/vipinsharma/interviewprep/config/AiConfig.java`

- [ ] **Step 1: Create AiConfig**

`src/main/java/com/vipinsharma/interviewprep/config/AiConfig.java`:

```java
package com.vipinsharma.interviewprep.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public InMemoryChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public ChatClient.Builder chatClientBuilder(org.springframework.ai.chat.model.ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
```

- [ ] **Step 2: Verify context loads**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.InterviewPrepAgentApplicationTests"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/vipinsharma/interviewprep/config/
git commit -m "feat: add AI configuration with ChatClient builder and InMemoryChatMemory"
```

---

### Task 6: ResearchAgent

**Files:**
- Create: `src/main/java/com/vipinsharma/interviewprep/agent/ResearchAgent.java`
- Test: `src/test/java/com/vipinsharma/interviewprep/agent/ResearchAgentTest.java`

- [ ] **Step 1: Write the failing test**

`src/test/java/com/vipinsharma/interviewprep/agent/ResearchAgentTest.java`:

```java
package com.vipinsharma.interviewprep.agent;

import com.vipinsharma.interviewprep.agent.tools.TavilySearchTool;
import com.vipinsharma.interviewprep.dto.CompanyBrief;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.chat.messages.AssistantMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResearchAgentTest {

    @Test
    void research_parsesCompanyBriefFromLlmResponse() {
        String llmResponse = """
            {
              "companySummary": "Stripe is a payments company",
              "techStack": ["Java", "Ruby", "Go"],
              "cultureSignals": "High ownership, direct feedback",
              "recentNews": "Stripe raised $6.5B",
              "likelyInterviewTopics": ["distributed systems", "API design"]
            }
            """;

        OpenAiChatModel mockModel = mock(OpenAiChatModel.class);
        when(mockModel.call(any(org.springframework.ai.chat.prompt.Prompt.class)))
            .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(llmResponse)))));

        ChatClient.Builder builder = ChatClient.builder(mockModel);
        TavilySearchTool tavilyTool = mock(TavilySearchTool.class);

        ResearchAgent agent = new ResearchAgent(builder, tavilyTool);
        CompanyBrief brief = agent.research("Stripe", "Senior SWE", "Build payment APIs");

        assertThat(brief.companyName()).isEqualTo("Stripe");
        assertThat(brief.techStack()).contains("Java");
        assertThat(brief.likelyInterviewTopics()).contains("distributed systems");
    }
}
```

Add `import java.util.List;` at the top of the test file.

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.agent.ResearchAgentTest"
```

Expected: FAIL — `ResearchAgent` not found.

- [ ] **Step 3: Implement ResearchAgent**

`src/main/java/com/vipinsharma/interviewprep/agent/ResearchAgent.java`:

```java
package com.vipinsharma.interviewprep.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipinsharma.interviewprep.agent.tools.TavilySearchTool;
import com.vipinsharma.interviewprep.dto.CompanyBrief;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResearchAgent {

    private static final String SYSTEM_PROMPT = """
        You are an expert research agent helping job seekers prepare for interviews.
        When given a company name and job title, you MUST:
        1. Search for the company's tech stack and engineering practices
        2. Search for the company's culture and values
        3. Search for recent news about the company
        4. Search for common interview topics at this company
        Make multiple searches to gather thorough information.
        Return ONLY a valid JSON object with these exact fields:
        {
          "companySummary": "string",
          "techStack": ["string"],
          "cultureSignals": "string",
          "recentNews": "string",
          "likelyInterviewTopics": ["string"]
        }
        No markdown, no explanation — only the JSON object.
        """;

    private final ChatClient chatClient;
    private final TavilySearchTool tavilySearchTool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResearchAgent(ChatClient.Builder chatClientBuilder, TavilySearchTool tavilySearchTool) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
        this.tavilySearchTool = tavilySearchTool;
    }

    public CompanyBrief research(String companyName, String jobTitle, String jobDescription) {
        String response = chatClient.prompt()
            .user("""
                Research %s for a %s position.
                Job description: %s
                """.formatted(companyName, jobTitle, jobDescription))
            .tools(tavilySearchTool)
            .call()
            .content();

        return parseCompanyBrief(companyName, response);
    }

    private CompanyBrief parseCompanyBrief(String companyName, String json) {
        try {
            var node = objectMapper.readTree(json);
            List<String> techStack = objectMapper.convertValue(
                node.get("techStack"), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class));
            List<String> topics = objectMapper.convertValue(
                node.get("likelyInterviewTopics"), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class));

            return new CompanyBrief(
                companyName,
                node.path("companySummary").asText(),
                techStack,
                node.path("cultureSignals").asText(),
                node.path("recentNews").asText(),
                topics
            );
        } catch (Exception e) {
            return new CompanyBrief(companyName, json, List.of(), "", "", List.of());
        }
    }
}
```

Add `com.fasterxml.jackson.databind:jackson-databind` is already included transitively via `spring-boot-starter-web`.

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.agent.ResearchAgentTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add src/
git commit -m "feat: add ResearchAgent with Tavily tool and JSON parsing"
```

---

### Task 7: InterviewerAgent

**Files:**
- Create: `src/main/java/com/vipinsharma/interviewprep/agent/InterviewerAgent.java`
- Test: `src/test/java/com/vipinsharma/interviewprep/agent/InterviewerAgentTest.java`

- [ ] **Step 1: Write the failing test**

`src/test/java/com/vipinsharma/interviewprep/agent/InterviewerAgentTest.java`:

```java
package com.vipinsharma.interviewprep.agent;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InterviewerAgentTest {

    @Test
    void chat_returnsInterviewerResponse() {
        String expectedReply = "Tell me about a time you designed a distributed system.";

        OpenAiChatModel mockModel = mock(OpenAiChatModel.class);
        when(mockModel.call(any(org.springframework.ai.chat.prompt.Prompt.class)))
            .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(expectedReply)))));

        ChatClient.Builder builder = ChatClient.builder(mockModel);
        InMemoryChatMemory memory = new InMemoryChatMemory();

        InterviewerAgent agent = new InterviewerAgent(builder, memory);
        UUID sessionId = UUID.randomUUID();
        String systemPrompt = "You are a senior interviewer at Stripe. Ask relevant questions.";

        String reply = agent.chat(sessionId, systemPrompt, "I am ready to start.");

        assertThat(reply).isEqualTo(expectedReply);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.agent.InterviewerAgentTest"
```

Expected: FAIL — `InterviewerAgent` not found.

- [ ] **Step 3: Implement InterviewerAgent**

`src/main/java/com/vipinsharma/interviewprep/agent/InterviewerAgent.java`:

```java
package com.vipinsharma.interviewprep.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Component
public class InterviewerAgent {

    private static final int MEMORY_WINDOW_SIZE = 20;

    private final ChatClient chatClient;
    private final InMemoryChatMemory chatMemory;

    public InterviewerAgent(ChatClient.Builder chatClientBuilder, InMemoryChatMemory chatMemory) {
        this.chatClient = chatClientBuilder.build();
        this.chatMemory = chatMemory;
    }

    public String chat(UUID sessionId, String systemPrompt, String userMessage) {
        return chatClient.prompt()
            .system(systemPrompt)
            .user(userMessage)
            .advisors(new MessageChatMemoryAdvisor(chatMemory))
            .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId.toString()))
            .call()
            .content();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.agent.InterviewerAgentTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add src/
git commit -m "feat: add InterviewerAgent with MessageChatMemoryAdvisor"
```

---

### Task 8: CoachAgent

**Files:**
- Create: `src/main/java/com/vipinsharma/interviewprep/agent/CoachAgent.java`
- Test: `src/test/java/com/vipinsharma/interviewprep/agent/CoachAgentTest.java`

- [ ] **Step 1: Write the failing test**

`src/test/java/com/vipinsharma/interviewprep/agent/CoachAgentTest.java`:

```java
package com.vipinsharma.interviewprep.agent;

import com.vipinsharma.interviewprep.dto.SessionFeedback;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CoachAgentTest {

    @Test
    void evaluate_parsesSessionFeedback() {
        UUID sessionId = UUID.randomUUID();
        String llmResponse = """
            {
              "overallScore": 3,
              "weakAreas": ["system design", "concurrency"],
              "improvementSuggestions": ["Study CAP theorem", "Practice LeetCode threading problems"],
              "detailedFeedback": "Good communication but shallow on technical depth."
            }
            """;

        OpenAiChatModel mockModel = mock(OpenAiChatModel.class);
        when(mockModel.call(any(org.springframework.ai.chat.prompt.Prompt.class)))
            .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(llmResponse)))));

        ChatClient.Builder builder = ChatClient.builder(mockModel);
        CoachAgent agent = new CoachAgent(builder);

        SessionFeedback feedback = agent.evaluate(sessionId, "transcript content", List.of("system design"));

        assertThat(feedback.overallScore()).isEqualTo(3);
        assertThat(feedback.weakAreas()).contains("system design");
        assertThat(feedback.sessionId()).isEqualTo(sessionId);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.agent.CoachAgentTest"
```

Expected: FAIL — `CoachAgent` not found.

- [ ] **Step 3: Implement CoachAgent**

`src/main/java/com/vipinsharma/interviewprep/agent/CoachAgent.java`:

```java
package com.vipinsharma.interviewprep.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipinsharma.interviewprep.dto.SessionFeedback;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CoachAgent {

    private static final String SYSTEM_PROMPT = """
        You are an expert interview coach. Analyze the provided interview transcript and give structured feedback.
        Return ONLY a valid JSON object with these exact fields:
        {
          "overallScore": number (1-5),
          "weakAreas": ["string"],
          "improvementSuggestions": ["string"],
          "detailedFeedback": "string"
        }
        No markdown, no explanation — only the JSON object.
        """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CoachAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
    }

    public SessionFeedback evaluate(UUID sessionId, String transcript, List<String> historicalWeakAreas) {
        String response = chatClient.prompt()
            .user("""
                Interview transcript:
                %s

                Historical weak areas from past sessions: %s

                Evaluate this interview and identify patterns.
                """.formatted(transcript, String.join(", ", historicalWeakAreas)))
            .call()
            .content();

        return parseFeedback(sessionId, response);
    }

    private SessionFeedback parseFeedback(UUID sessionId, String json) {
        try {
            var node = objectMapper.readTree(json);
            List<String> weakAreas = objectMapper.convertValue(
                node.get("weakAreas"), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class));
            List<String> suggestions = objectMapper.convertValue(
                node.get("improvementSuggestions"), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class));

            return new SessionFeedback(
                sessionId,
                node.path("overallScore").asInt(),
                weakAreas,
                suggestions,
                node.path("detailedFeedback").asText()
            );
        } catch (Exception e) {
            return new SessionFeedback(sessionId, 0, List.of(), List.of(), json);
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.agent.CoachAgentTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add src/
git commit -m "feat: add CoachAgent for session evaluation and feedback"
```

---

### Task 9: ResearchService

**Files:**
- Create: `src/main/java/com/vipinsharma/interviewprep/service/ResearchService.java`
- Test: `src/test/java/com/vipinsharma/interviewprep/service/ResearchServiceTest.java`

- [ ] **Step 1: Write the failing test**

`src/test/java/com/vipinsharma/interviewprep/service/ResearchServiceTest.java`:

```java
package com.vipinsharma.interviewprep.service;

import com.vipinsharma.interviewprep.agent.ResearchAgent;
import com.vipinsharma.interviewprep.dto.CompanyBrief;
import com.vipinsharma.interviewprep.dto.ResearchRequest;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResearchServiceTest {

    @Mock ResearchAgent researchAgent;
    @Mock SessionRepository sessionRepository;
    @InjectMocks ResearchService researchService;

    @Test
    void research_callsAgentAndPersistsSession() {
        ResearchRequest request = new ResearchRequest("Stripe", "Senior SWE", "Build APIs");
        CompanyBrief brief = new CompanyBrief(
            "Stripe", "Payments company", List.of("Java", "Go"),
            "High ownership", "Raised $6.5B", List.of("distributed systems")
        );

        when(researchAgent.research(eq("Stripe"), eq("Senior SWE"), eq("Build APIs")))
            .thenReturn(brief);
        when(sessionRepository.save(any(Session.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        CompanyBrief result = researchService.research(request);

        assertThat(result.companyName()).isEqualTo("Stripe");
        verify(sessionRepository).save(any(Session.class));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.service.ResearchServiceTest"
```

Expected: FAIL — `ResearchService` not found.

- [ ] **Step 3: Implement ResearchService**

`src/main/java/com/vipinsharma/interviewprep/service/ResearchService.java`:

```java
package com.vipinsharma.interviewprep.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipinsharma.interviewprep.agent.ResearchAgent;
import com.vipinsharma.interviewprep.dto.CompanyBrief;
import com.vipinsharma.interviewprep.dto.ResearchRequest;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import org.springframework.stereotype.Service;

@Service
public class ResearchService {

    private final ResearchAgent researchAgent;
    private final SessionRepository sessionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResearchService(ResearchAgent researchAgent, SessionRepository sessionRepository) {
        this.researchAgent = researchAgent;
        this.sessionRepository = sessionRepository;
    }

    public CompanyBrief research(ResearchRequest request) {
        CompanyBrief brief = researchAgent.research(
            request.companyName(), request.jobTitle(), request.jobDescription());

        Session session = new Session();
        session.setCompanyName(request.companyName());
        session.setJobTitle(request.jobTitle());
        session.setCompanyBrief(toJson(brief));
        session.setStatus("RESEARCH_COMPLETE");
        sessionRepository.save(session);

        return brief;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.service.ResearchServiceTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add src/
git commit -m "feat: add ResearchService"
```

---

### Task 10: InterviewSessionService

**Files:**
- Create: `src/main/java/com/vipinsharma/interviewprep/service/InterviewSessionService.java`
- Test: `src/test/java/com/vipinsharma/interviewprep/service/InterviewSessionServiceTest.java`

- [ ] **Step 1: Write the failing test**

`src/test/java/com/vipinsharma/interviewprep/service/InterviewSessionServiceTest.java`:

```java
package com.vipinsharma.interviewprep.service;

import com.vipinsharma.interviewprep.dto.StartSessionRequest;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.model.WeakArea;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import com.vipinsharma.interviewprep.repository.WeakAreaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewSessionServiceTest {

    @Mock SessionRepository sessionRepository;
    @Mock WeakAreaRepository weakAreaRepository;
    @InjectMocks InterviewSessionService service;

    @Test
    void startSession_createsActiveSession() {
        UUID researchSessionId = UUID.randomUUID();
        Session researchSession = new Session();
        researchSession.setId(researchSessionId);
        researchSession.setCompanyName("Google");
        researchSession.setJobTitle("Staff SWE");
        researchSession.setCompanyBrief("{\"companySummary\":\"Search engine\"}");

        when(sessionRepository.findById(researchSessionId)).thenReturn(Optional.of(researchSession));
        when(weakAreaRepository.findTop5ByFrequency()).thenReturn(List.of());
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StartSessionRequest request = new StartSessionRequest(researchSessionId, "Build search infra");
        Session result = service.startSession(request);

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getCompanyName()).isEqualTo("Google");
    }

    @Test
    void startSession_throwsWhenResearchNotFound() {
        when(sessionRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startSession(
            new StartSessionRequest(UUID.randomUUID(), "job desc")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Research session not found");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.service.InterviewSessionServiceTest"
```

Expected: FAIL — `InterviewSessionService` not found.

- [ ] **Step 3: Implement InterviewSessionService**

`src/main/java/com/vipinsharma/interviewprep/service/InterviewSessionService.java`:

```java
package com.vipinsharma.interviewprep.service;

import com.vipinsharma.interviewprep.dto.StartSessionRequest;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.model.WeakArea;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import com.vipinsharma.interviewprep.repository.WeakAreaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewSessionService {

    private final SessionRepository sessionRepository;
    private final WeakAreaRepository weakAreaRepository;

    public InterviewSessionService(SessionRepository sessionRepository,
                                   WeakAreaRepository weakAreaRepository) {
        this.sessionRepository = sessionRepository;
        this.weakAreaRepository = weakAreaRepository;
    }

    public Session startSession(StartSessionRequest request) {
        Session researchSession = sessionRepository.findById(request.companyBriefSessionId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Research session not found: " + request.companyBriefSessionId()));

        List<WeakArea> topWeakAreas = weakAreaRepository.findTop5ByFrequency();
        String weakAreaContext = topWeakAreas.isEmpty() ? "none yet" :
            topWeakAreas.stream().map(WeakArea::getTopic).collect(Collectors.joining(", "));

        Session interviewSession = new Session();
        interviewSession.setCompanyName(researchSession.getCompanyName());
        interviewSession.setJobTitle(researchSession.getJobTitle());
        interviewSession.setCompanyBrief(researchSession.getCompanyBrief());
        interviewSession.setStatus("ACTIVE");

        return sessionRepository.save(interviewSession);
    }

    public String buildSystemPrompt(Session session) {
        List<WeakArea> topWeakAreas = weakAreaRepository.findTop5ByFrequency();
        String weakAreaContext = topWeakAreas.isEmpty() ? "none identified yet" :
            topWeakAreas.stream().map(WeakArea::getTopic).collect(Collectors.joining(", "));

        return """
            You are a senior interviewer at %s conducting an interview for a %s role.
            Company context: %s
            Historical weak areas for this candidate: %s
            Ask relevant, probing questions. Focus extra attention on the weak areas.
            Start with a brief introduction, then ask your first question.
            """.formatted(
                session.getCompanyName(),
                session.getJobTitle(),
                session.getCompanyBrief(),
                weakAreaContext);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.service.InterviewSessionServiceTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add src/
git commit -m "feat: add InterviewSessionService with weak area context injection"
```

---

### Task 11: ChatService

**Files:**
- Create: `src/main/java/com/vipinsharma/interviewprep/service/ChatService.java`
- Test: `src/test/java/com/vipinsharma/interviewprep/service/ChatServiceTest.java`

- [ ] **Step 1: Write the failing test**

`src/test/java/com/vipinsharma/interviewprep/service/ChatServiceTest.java`:

```java
package com.vipinsharma.interviewprep.service;

import com.vipinsharma.interviewprep.agent.InterviewerAgent;
import com.vipinsharma.interviewprep.dto.ChatResponse;
import com.vipinsharma.interviewprep.model.Message;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.repository.MessageRepository;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock InterviewerAgent interviewerAgent;
    @Mock InterviewSessionService interviewSessionService;
    @Mock SessionRepository sessionRepository;
    @Mock MessageRepository messageRepository;
    @InjectMocks ChatService chatService;

    @Test
    void chat_persistsMessagesAndReturnsReply() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setStatus("ACTIVE");
        session.setCompanyName("Netflix");
        session.setJobTitle("Senior SWE");
        session.setCompanyBrief("{}");

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(interviewSessionService.buildSystemPrompt(session))
            .thenReturn("You are an interviewer at Netflix");
        when(interviewerAgent.chat(eq(sessionId), anyString(), eq("My answer")))
            .thenReturn("Great. What about system design?");
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        ChatResponse response = chatService.chat(sessionId, "My answer");

        assertThat(response.reply()).isEqualTo("Great. What about system design?");
        verify(messageRepository, times(2)).save(any(Message.class));
    }

    @Test
    void chat_throwsWhenSessionNotFound() {
        when(sessionRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.chat(UUID.randomUUID(), "hello"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.service.ChatServiceTest"
```

Expected: FAIL — `ChatService` not found.

- [ ] **Step 3: Implement ChatService**

`src/main/java/com/vipinsharma/interviewprep/service/ChatService.java`:

```java
package com.vipinsharma.interviewprep.service;

import com.vipinsharma.interviewprep.agent.InterviewerAgent;
import com.vipinsharma.interviewprep.dto.ChatResponse;
import com.vipinsharma.interviewprep.model.Message;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.repository.MessageRepository;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChatService {

    private final InterviewerAgent interviewerAgent;
    private final InterviewSessionService interviewSessionService;
    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;

    public ChatService(InterviewerAgent interviewerAgent,
                       InterviewSessionService interviewSessionService,
                       SessionRepository sessionRepository,
                       MessageRepository messageRepository) {
        this.interviewerAgent = interviewerAgent;
        this.interviewSessionService = interviewSessionService;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    public ChatResponse chat(UUID sessionId, String userMessage) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        persist(session, "USER", userMessage);

        String systemPrompt = interviewSessionService.buildSystemPrompt(session);
        String reply = interviewerAgent.chat(sessionId, systemPrompt, userMessage);

        persist(session, "ASSISTANT", reply);

        return new ChatResponse(reply);
    }

    private void persist(Session session, String role, String content) {
        Message message = new Message();
        message.setSession(session);
        message.setRole(role);
        message.setContent(content);
        messageRepository.save(message);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.service.ChatServiceTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add src/
git commit -m "feat: add ChatService for multi-turn conversation with message persistence"
```

---

### Task 12: ScoringService

**Files:**
- Create: `src/main/java/com/vipinsharma/interviewprep/service/ScoringService.java`
- Test: `src/test/java/com/vipinsharma/interviewprep/service/ScoringServiceTest.java`

- [ ] **Step 1: Write the failing test**

`src/test/java/com/vipinsharma/interviewprep/service/ScoringServiceTest.java`:

```java
package com.vipinsharma.interviewprep.service;

import com.vipinsharma.interviewprep.agent.CoachAgent;
import com.vipinsharma.interviewprep.dto.SessionFeedback;
import com.vipinsharma.interviewprep.model.Message;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.model.WeakArea;
import com.vipinsharma.interviewprep.repository.MessageRepository;
import com.vipinsharma.interviewprep.repository.ScoreRepository;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import com.vipinsharma.interviewprep.repository.WeakAreaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoringServiceTest {

    @Mock CoachAgent coachAgent;
    @Mock SessionRepository sessionRepository;
    @Mock MessageRepository messageRepository;
    @Mock ScoreRepository scoreRepository;
    @Mock WeakAreaRepository weakAreaRepository;
    @InjectMocks ScoringService scoringService;

    @Test
    void evaluate_callsCoachAndPersistsScoreAndWeakAreas() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);

        Message msg1 = new Message();
        msg1.setRole("ASSISTANT");
        msg1.setContent("Tell me about distributed systems.");
        Message msg2 = new Message();
        msg2.setRole("USER");
        msg2.setContent("I built a Kafka-based pipeline.");

        SessionFeedback feedback = new SessionFeedback(
            sessionId, 4,
            List.of("system design"),
            List.of("Study CAP theorem"),
            "Good answer"
        );

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId))
            .thenReturn(List.of(msg1, msg2));
        when(weakAreaRepository.findTop5ByFrequency()).thenReturn(List.of());
        when(coachAgent.evaluate(eq(sessionId), anyString(), any())).thenReturn(feedback);
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(weakAreaRepository.findByTopic("system design")).thenReturn(Optional.empty());
        when(weakAreaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SessionFeedback result = scoringService.evaluate(sessionId);

        assertThat(result.overallScore()).isEqualTo(4);
        verify(scoreRepository).save(any());
        verify(weakAreaRepository).save(any());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.service.ScoringServiceTest"
```

Expected: FAIL — `ScoringService` not found.

- [ ] **Step 3: Implement ScoringService**

`src/main/java/com/vipinsharma/interviewprep/service/ScoringService.java`:

```java
package com.vipinsharma.interviewprep.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipinsharma.interviewprep.agent.CoachAgent;
import com.vipinsharma.interviewprep.dto.SessionFeedback;
import com.vipinsharma.interviewprep.model.Message;
import com.vipinsharma.interviewprep.model.Score;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.model.WeakArea;
import com.vipinsharma.interviewprep.repository.MessageRepository;
import com.vipinsharma.interviewprep.repository.ScoreRepository;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import com.vipinsharma.interviewprep.repository.WeakAreaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ScoringService {

    private final CoachAgent coachAgent;
    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final ScoreRepository scoreRepository;
    private final WeakAreaRepository weakAreaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScoringService(CoachAgent coachAgent, SessionRepository sessionRepository,
                          MessageRepository messageRepository, ScoreRepository scoreRepository,
                          WeakAreaRepository weakAreaRepository) {
        this.coachAgent = coachAgent;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.scoreRepository = scoreRepository;
        this.weakAreaRepository = weakAreaRepository;
    }

    public SessionFeedback evaluate(UUID sessionId) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        List<Message> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        String transcript = messages.stream()
            .map(m -> m.getRole() + ": " + m.getContent())
            .collect(Collectors.joining("\n"));

        List<WeakArea> historical = weakAreaRepository.findTop5ByFrequency();
        List<String> historicalTopics = historical.stream()
            .map(WeakArea::getTopic).toList();

        SessionFeedback feedback = coachAgent.evaluate(sessionId, transcript, historicalTopics);

        persistScore(session, feedback);
        updateWeakAreas(feedback.weakAreas());
        markSessionCompleted(session);

        return feedback;
    }

    private void persistScore(Session session, SessionFeedback feedback) {
        Score score = new Score();
        score.setSession(session);
        score.setOverallScore(feedback.overallScore());
        try {
            score.setFeedback(objectMapper.writeValueAsString(feedback));
        } catch (Exception e) {
            score.setFeedback(feedback.detailedFeedback());
        }
        scoreRepository.save(score);
    }

    private void updateWeakAreas(List<String> topics) {
        for (String topic : topics) {
            WeakArea area = weakAreaRepository.findByTopic(topic)
                .orElseGet(() -> {
                    WeakArea w = new WeakArea();
                    w.setTopic(topic);
                    w.setFrequency(0);
                    return w;
                });
            area.setFrequency(area.getFrequency() + 1);
            area.setLastSeen(LocalDateTime.now());
            weakAreaRepository.save(area);
        }
    }

    private void markSessionCompleted(Session session) {
        session.setStatus("COMPLETED");
        sessionRepository.save(session);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.service.ScoringServiceTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add src/
git commit -m "feat: add ScoringService with weak area tracking"
```

---

### Task 13: SessionHistoryService

**Files:**
- Create: `src/main/java/com/vipinsharma/interviewprep/service/SessionHistoryService.java`
- Test: `src/test/java/com/vipinsharma/interviewprep/service/SessionHistoryServiceTest.java`

- [ ] **Step 1: Write the failing test**

`src/test/java/com/vipinsharma/interviewprep/service/SessionHistoryServiceTest.java`:

```java
package com.vipinsharma.interviewprep.service;

import com.vipinsharma.interviewprep.dto.SessionSummary;
import com.vipinsharma.interviewprep.model.Score;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.repository.ScoreRepository;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionHistoryServiceTest {

    @Mock SessionRepository sessionRepository;
    @Mock ScoreRepository scoreRepository;
    @InjectMocks SessionHistoryService historyService;

    @Test
    void listSessions_returnsSummariesWithScores() {
        Session s1 = new Session();
        s1.setId(UUID.randomUUID());
        s1.setCompanyName("Apple");
        s1.setJobTitle("Staff SWE");
        s1.setStatus("COMPLETED");

        Score score = new Score();
        score.setOverallScore(4);

        when(sessionRepository.findAll()).thenReturn(List.of(s1));
        when(scoreRepository.findBySessionId(s1.getId())).thenReturn(Optional.of(score));

        List<SessionSummary> summaries = historyService.listSessions();

        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).companyName()).isEqualTo("Apple");
        assertThat(summaries.get(0).overallScore()).isEqualTo(4);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.service.SessionHistoryServiceTest"
```

Expected: FAIL — `SessionHistoryService` not found.

- [ ] **Step 3: Implement SessionHistoryService**

`src/main/java/com/vipinsharma/interviewprep/service/SessionHistoryService.java`:

```java
package com.vipinsharma.interviewprep.service;

import com.vipinsharma.interviewprep.dto.SessionSummary;
import com.vipinsharma.interviewprep.model.Score;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.repository.ScoreRepository;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionHistoryService {

    private final SessionRepository sessionRepository;
    private final ScoreRepository scoreRepository;

    public SessionHistoryService(SessionRepository sessionRepository, ScoreRepository scoreRepository) {
        this.sessionRepository = sessionRepository;
        this.scoreRepository = scoreRepository;
    }

    public List<SessionSummary> listSessions() {
        return sessionRepository.findAll().stream()
            .map(this::toSummary)
            .toList();
    }

    private SessionSummary toSummary(Session session) {
        Integer score = scoreRepository.findBySessionId(session.getId())
            .map(Score::getOverallScore)
            .orElse(null);

        return new SessionSummary(
            session.getId(),
            session.getCompanyName(),
            session.getJobTitle(),
            session.getStatus(),
            score,
            session.getCreatedAt()
        );
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.service.SessionHistoryServiceTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add src/
git commit -m "feat: add SessionHistoryService"
```

---

### Task 14: REST controllers

**Files:**
- Create: `src/main/java/com/vipinsharma/interviewprep/api/ResearchController.java`
- Create: `src/main/java/com/vipinsharma/interviewprep/api/SessionController.java`
- Test: `src/test/java/com/vipinsharma/interviewprep/api/ResearchControllerTest.java`
- Test: `src/test/java/com/vipinsharma/interviewprep/api/SessionControllerTest.java`

- [ ] **Step 1: Write the failing controller tests**

`src/test/java/com/vipinsharma/interviewprep/api/ResearchControllerTest.java`:

```java
package com.vipinsharma.interviewprep.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipinsharma.interviewprep.dto.CompanyBrief;
import com.vipinsharma.interviewprep.dto.ResearchRequest;
import com.vipinsharma.interviewprep.service.ResearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResearchController.class)
class ResearchControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ResearchService researchService;

    @Test
    void postResearch_returns200WithCompanyBrief() throws Exception {
        ResearchRequest request = new ResearchRequest("Stripe", "Senior SWE", "Build APIs");
        CompanyBrief brief = new CompanyBrief(
            "Stripe", "Payments", List.of("Java"), "High ownership", "Recent news", List.of("system design")
        );

        when(researchService.research(any())).thenReturn(brief);

        mockMvc.perform(post("/api/research")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.companyName").value("Stripe"))
            .andExpect(jsonPath("$.companySummary").value("Payments"));
    }
}
```

`src/test/java/com/vipinsharma/interviewprep/api/SessionControllerTest.java`:

```java
package com.vipinsharma.interviewprep.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipinsharma.interviewprep.dto.ChatRequest;
import com.vipinsharma.interviewprep.dto.ChatResponse;
import com.vipinsharma.interviewprep.dto.SessionFeedback;
import com.vipinsharma.interviewprep.dto.SessionSummary;
import com.vipinsharma.interviewprep.dto.StartSessionRequest;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.service.ChatService;
import com.vipinsharma.interviewprep.service.InterviewSessionService;
import com.vipinsharma.interviewprep.service.ScoringService;
import com.vipinsharma.interviewprep.service.SessionHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionController.class)
class SessionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean InterviewSessionService interviewSessionService;
    @MockBean ChatService chatService;
    @MockBean ScoringService scoringService;
    @MockBean SessionHistoryService sessionHistoryService;

    @Test
    void postSessions_returns201WithSessionId() throws Exception {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setCompanyName("Netflix");
        session.setJobTitle("Senior SWE");
        session.setStatus("ACTIVE");

        when(interviewSessionService.startSession(any())).thenReturn(session);

        StartSessionRequest request = new StartSessionRequest(UUID.randomUUID(), "Build streaming infra");

        mockMvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.sessionId").value(sessionId.toString()));
    }

    @Test
    void postChat_returns200WithReply() throws Exception {
        UUID sessionId = UUID.randomUUID();
        when(chatService.chat(eq(sessionId), eq("My answer")))
            .thenReturn(new ChatResponse("Great, follow up question."));

        mockMvc.perform(post("/api/sessions/{id}/chat", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ChatRequest("My answer"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reply").value("Great, follow up question."));
    }

    @Test
    void getSessions_returns200WithList() throws Exception {
        SessionSummary summary = new SessionSummary(
            UUID.randomUUID(), "Apple", "Staff SWE", "COMPLETED", 4, null);

        when(sessionHistoryService.listSessions()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/sessions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].companyName").value("Apple"));
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
./gradlew test --tests "com.vipinsharma.interviewprep.api.*"
```

Expected: FAIL — controllers not found.

- [ ] **Step 3: Implement ResearchController**

`src/main/java/com/vipinsharma/interviewprep/api/ResearchController.java`:

```java
package com.vipinsharma.interviewprep.api;

import com.vipinsharma.interviewprep.dto.CompanyBrief;
import com.vipinsharma.interviewprep.dto.ResearchRequest;
import com.vipinsharma.interviewprep.service.ResearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/research")
public class ResearchController {

    private final ResearchService researchService;

    public ResearchController(ResearchService researchService) {
        this.researchService = researchService;
    }

    @PostMapping
    public ResponseEntity<CompanyBrief> research(@RequestBody ResearchRequest request) {
        return ResponseEntity.ok(researchService.research(request));
    }
}
```

- [ ] **Step 4: Implement SessionController**

`src/main/java/com/vipinsharma/interviewprep/api/SessionController.java`:

```java
package com.vipinsharma.interviewprep.api;

import com.vipinsharma.interviewprep.dto.*;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final InterviewSessionService interviewSessionService;
    private final ChatService chatService;
    private final ScoringService scoringService;
    private final SessionHistoryService sessionHistoryService;

    public SessionController(InterviewSessionService interviewSessionService,
                             ChatService chatService,
                             ScoringService scoringService,
                             SessionHistoryService sessionHistoryService) {
        this.interviewSessionService = interviewSessionService;
        this.chatService = chatService;
        this.scoringService = scoringService;
        this.sessionHistoryService = sessionHistoryService;
    }

    @PostMapping
    public ResponseEntity<Map<String, UUID>> startSession(@RequestBody StartSessionRequest request) {
        Session session = interviewSessionService.startSession(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("sessionId", session.getId()));
    }

    @PostMapping("/{id}/chat")
    public ResponseEntity<ChatResponse> chat(
            @PathVariable UUID id,
            @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.chat(id, request.message()));
    }

    @PostMapping("/{id}/evaluate")
    public ResponseEntity<SessionFeedback> evaluate(@PathVariable UUID id) {
        return ResponseEntity.ok(scoringService.evaluate(id));
    }

    @GetMapping
    public ResponseEntity<List<SessionSummary>> listSessions() {
        return ResponseEntity.ok(sessionHistoryService.listSessions());
    }
}
```

- [ ] **Step 5: Run all tests**

```bash
./gradlew test
```

Expected: `BUILD SUCCESSFUL` — all tests pass.

- [ ] **Step 6: Commit**

```bash
git add src/
git commit -m "feat: add REST controllers for research and session management"
```

---

### Task 15: GitHub Actions CI/CD

**Files:**
- Create: `.github/workflows/deploy.yml`

- [ ] **Step 1: Create the workflow directory**

```bash
mkdir -p ~/Projects/interview-prep-agent/.github/workflows
```

- [ ] **Step 2: Create deploy.yml**

`.github/workflows/deploy.yml`:

```yaml
name: CI/CD — Build, Test, Deploy

on:
  push:
    branches: [ main ]

jobs:
  build-test-deploy:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Cache Gradle packages
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}

      - name: Run tests
        run: ./gradlew test
        env:
          OPENAI_API_KEY: test-key
          TAVILY_API_KEY: test-key

      - name: Build JAR
        run: ./gradlew bootJar
        env:
          OPENAI_API_KEY: test-key
          TAVILY_API_KEY: test-key

      - name: Set up SSH
        uses: webfactory/ssh-agent@v0.9.0
        with:
          ssh-private-key: ${{ secrets.ORACLE_SSH_KEY }}

      - name: Add Oracle VM to known hosts
        run: |
          ssh-keyscan -H ${{ secrets.ORACLE_HOST }} >> ~/.ssh/known_hosts

      - name: Copy JAR to Oracle VM
        run: |
          scp build/libs/interview-prep-agent-*.jar \
            ${{ secrets.ORACLE_SSH_USER }}@${{ secrets.ORACLE_HOST }}:~/app/interview-prep-agent.jar

      - name: Restart service on Oracle VM
        run: |
          ssh ${{ secrets.ORACLE_SSH_USER }}@${{ secrets.ORACLE_HOST }} \
            "sudo systemctl restart interview-prep-agent"
```

- [ ] **Step 3: Commit**

```bash
git add .github/
git commit -m "ci: add GitHub Actions workflow for build, test, and deploy to Oracle VM"
```

---

### Task 16: Deployment configuration

**Files:**
- Create: `deploy/interview-prep-agent.service`
- Create: `deploy/Caddyfile`
- Create: `deploy/README.md`

- [ ] **Step 1: Create systemd service file**

`deploy/interview-prep-agent.service`:

```ini
[Unit]
Description=Interview Prep Agent
After=network.target

[Service]
User=opc
WorkingDirectory=/home/opc/app
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /home/opc/app/interview-prep-agent.jar
EnvironmentFile=/home/opc/app/.env
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

- [ ] **Step 2: Create Caddyfile**

`deploy/Caddyfile`:

```
your-domain-or-ip {
    reverse_proxy localhost:8080
}
```

Replace `your-domain-or-ip` with your Oracle VM's public IP or domain name. Caddy will automatically obtain a TLS certificate if you use a domain.

- [ ] **Step 3: Create deployment README**

`deploy/README.md`:

```markdown
# Oracle VM Setup (one-time)

## 1. Provision Ampere A1 VM on Oracle Free Tier
- Shape: VM.Standard.A1.Flex — 4 OCPUs, 24 GB RAM
- OS: Ubuntu 22.04
- Open ports 22, 80, 443 in the security list

## 2. Install dependencies on the VM

ssh opc@<your-vm-ip>

sudo apt update && sudo apt install -y openjdk-21-jdk postgresql caddy

## 3. Create the database

sudo -u postgres psql -c "CREATE DATABASE interviewdb;"
sudo -u postgres psql -c "CREATE USER interviewuser WITH PASSWORD 'yourpassword';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE interviewdb TO interviewuser;"

## 4. Create the app directory and .env file

mkdir -p ~/app
cat > ~/app/.env <<EOF
OPENAI_API_KEY=your-openai-key
TAVILY_API_KEY=your-tavily-key
DB_HOST=localhost
DB_USER=interviewuser
DB_PASSWORD=yourpassword
EOF
chmod 600 ~/app/.env

## 5. Install the systemd service

sudo cp deploy/interview-prep-agent.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable interview-prep-agent

## 6. Configure Caddy

sudo cp deploy/Caddyfile /etc/caddy/Caddyfile
sudo systemctl restart caddy

## 7. Add GitHub Secrets

In your GitHub repo → Settings → Secrets and variables → Actions:
- ORACLE_HOST: <your-vm-public-ip>
- ORACLE_SSH_USER: opc
- ORACLE_SSH_KEY: <contents of your SSH private key>

## 8. First deploy

Push to main — GitHub Actions will build, test, and deploy automatically.
Verify: curl http://<your-vm-ip>/api/sessions
```

- [ ] **Step 4: Commit**

```bash
git add deploy/
git commit -m "docs: add Oracle VM deployment config and setup instructions"
```

---

### Task 17: Full test run and final verification

- [ ] **Step 1: Run full test suite**

```bash
cd ~/Projects/interview-prep-agent
./gradlew clean test
```

Expected: `BUILD SUCCESSFUL` — all tests pass, zero failures.

- [ ] **Step 2: Verify the JAR builds**

```bash
./gradlew bootJar
ls -lh build/libs/
```

Expected: `interview-prep-agent-0.0.1-SNAPSHOT.jar` present, size > 50 MB (fat JAR).

- [ ] **Step 3: Smoke test local startup**

```bash
OPENAI_API_KEY=test TAVILY_API_KEY=test ./gradlew bootRun &
sleep 10
curl -s http://localhost:8080/api/sessions
kill %1
```

Expected: `[]` — empty JSON array, server started cleanly on dev profile.

- [ ] **Step 4: Final commit**

```bash
git add .
git commit -m "chore: final build verification — all tests passing"
```

---

## Self-Review Checklist

**Spec coverage:**
- ✅ ResearchAgent with TavilySearchTool — Task 4, 6
- ✅ InterviewerAgent with memory — Task 7
- ✅ CoachAgent — Task 8
- ✅ All 5 services — Tasks 9–13
- ✅ All 6 REST endpoints — Task 14
- ✅ H2 dev / PostgreSQL prod via profiles — Task 1
- ✅ Java records for all DTOs — Task 3
- ✅ GitHub Actions CI/CD — Task 15
- ✅ systemd + Caddy deployment — Task 16
- ✅ weak_areas cross-session memory — Tasks 2, 12, 13

**Placeholder scan:** No TBD, no TODOs. All code is complete. `deploy/Caddyfile` has one intentional placeholder (`your-domain-or-ip`) with explicit instructions.

**Type consistency:**
- `CompanyBrief` record fields match usage in `ResearchAgent`, `ResearchService`, `ResearchController`
- `SessionFeedback` record fields match `CoachAgent` output and `ScoringService` persistence
- `MessageRepository.findBySessionIdOrderByCreatedAtAsc` defined in Task 2, used in Task 12
- `WeakAreaRepository.findTop5ByFrequency` defined in Task 2, used in Tasks 10 and 12
- `InterviewSessionService.buildSystemPrompt` defined in Task 10, used in Task 11
