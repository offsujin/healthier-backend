package com.healthier.diagnosis.controller;

import com.healthier.diagnosis.dto.QuestionRequestDto;
import com.healthier.diagnosis.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(value="/api/diagnose")
@RestController
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping (value = "/sleepdisorder")
    public ResponseEntity<?> getNextQuestion(@RequestBody @Valid QuestionRequestDto dto) {
        System.out.println("=============================");
        System.out.println(dto);
        return ResponseEntity.ok(questionService.findNextQuestion(dto));
    }
}