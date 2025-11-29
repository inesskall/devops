package dev.yerassyl.aliyev.service;

import dev.yerassyl.aliyev.dto.FeedbackRequest;

import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с обратной связью
 */
public interface FeedbackService {
    /**
     * Сохранить обратную связь в файл
     * 
     * @param feedbackRequest запрос с данными обратной связи
     */
    void saveFeedback(FeedbackRequest feedbackRequest);
    
    /**
     * Получить все сообщения обратной связи
     * 
     * @return список всех сообщений обратной связи
     */
    List<Map<String, Object>> getAllFeedbacks();
}

