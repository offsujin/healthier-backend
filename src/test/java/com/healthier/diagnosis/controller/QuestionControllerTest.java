package com.healthier.diagnosis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthier.diagnosis.dto.QuestionRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class QuestionControllerTest {

    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void before(WebApplicationContext wac) {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();
    }

    @DisplayName("질문 조회")
    @Test
    void getNextQuestion() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/diagnose/sleepdisorder").contentType(MediaType.APPLICATION_JSON)
                .content(
                        objectMapper.writeValueAsString(new QuestionRequestDto(
                                "62c7973f9d8ed7017f145e1b",
                                2
                                )
                        )))
                .andExpect(status().isOk());

    }
}