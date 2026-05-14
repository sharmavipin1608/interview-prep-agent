package com.vipinsharma.interviewprep.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.vipinsharma.interviewprep.model.WeakArea;

@DataJpaTest
class WeakAreaRepositoryTest {

    @Autowired WeakAreaRepository weakAreaRepository;

    @Test
    void findByTopic_afterSave_returnsMatchingWeakArea() {
        WeakArea weakArea = new WeakArea();
        weakArea.setId(UUID.randomUUID());
        weakArea.setTopic("Dynamic Programming");
        weakArea.setFrequency(3);
        weakAreaRepository.save(weakArea);

        Optional<WeakArea> found = weakAreaRepository.findByTopic("Dynamic Programming");

        assertThat(found).isPresent();
    }

    @Test
    void findTop5ByOrderByFrequencyDesc_withSixWeakAreas_returnsOnlyFive() {
        for (int i = 1; i <= 6; i++) {
            WeakArea weakArea = new WeakArea();
            weakArea.setId(UUID.randomUUID());
            weakArea.setTopic("Topic-" + i);
            weakArea.setFrequency(i);
            weakAreaRepository.save(weakArea);
        }

        List<WeakArea> top5 = weakAreaRepository.findTop5ByOrderByFrequencyDesc();

        assertThat(top5).hasSize(5);
    }

    @Test
    void findTop5ByOrderByFrequencyDesc_withSixWeakAreas_returnsHighestFrequenciesFirst() {
        for (int i = 1; i <= 6; i++) {
            WeakArea weakArea = new WeakArea();
            weakArea.setId(UUID.randomUUID());
            weakArea.setTopic("Topic-" + i);
            weakArea.setFrequency(i);
            weakAreaRepository.save(weakArea);
        }

        List<WeakArea> top5 = weakAreaRepository.findTop5ByOrderByFrequencyDesc();

        assertThat(top5.get(0).getFrequency()).isEqualTo(6);
    }
}
