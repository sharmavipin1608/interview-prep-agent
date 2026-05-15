package com.vipinsharma.interviewprep.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SpaFallbackController.class)
class SpaFallbackControllerTest {

    @Autowired MockMvc mockMvc;

    @Test
    void unknownPath_forwardsToIndex() throws Exception {
        mockMvc.perform(get("/sessions/some-uuid/results"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }
}
