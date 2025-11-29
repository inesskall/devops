package dev.yerassyl.aliyev.controller;

import dev.yerassyl.aliyev.dto.FeedbackRequest;
import dev.yerassyl.aliyev.dto.SuccessEntity;
import dev.yerassyl.aliyev.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Контроллер для обработки обратной связи
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class FeedbackController {
    
    private final FeedbackService feedbackService;
    
    /**
     * Endpoint для отправки обратной связи
     * 
     * @param feedbackRequest данные обратной связи
     * @return SuccessEntity
     */
    @PostMapping(value = "/feedback", produces = "application/json")
    public ResponseEntity<SuccessEntity> submitFeedback(@RequestBody @Valid FeedbackRequest feedbackRequest) {
        log.info("Получена обратная связь от: {} ({})", feedbackRequest.getName(), feedbackRequest.getEmail());
        
        feedbackService.saveFeedback(feedbackRequest);
        
        SuccessEntity successEntity = new SuccessEntity();
        successEntity.setSuccess(true);
        
        return ResponseEntity.ok(successEntity);
    }
    
    /**
     * Endpoint для получения всех сообщений обратной связи
     * 
     * @return список всех сообщений обратной связи
     */
    @GetMapping(value = "/feedback", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> getAllFeedbacks() {
        log.info("Запрос на получение всех сообщений обратной связи");
        
        List<Map<String, Object>> feedbacks = feedbackService.getAllFeedbacks();
        
        return ResponseEntity.ok(feedbacks);
    }
}

