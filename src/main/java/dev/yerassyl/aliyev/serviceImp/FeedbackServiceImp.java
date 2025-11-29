package dev.yerassyl.aliyev.serviceImp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.yerassyl.aliyev.dto.FeedbackRequest;
import dev.yerassyl.aliyev.service.FeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация сервиса для работы с обратной связью
 * Сохраняет сообщения в JSON файлы в папке feedback/
 */
@Slf4j
@Service
public class FeedbackServiceImp implements FeedbackService {
    
    private static final String FEEDBACK_DIR = "feedback";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ObjectMapper objectMapper;
    
    public FeedbackServiceImp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Создаем папку feedback если её нет
        createFeedbackDirectory();
    }
    
    @Override
    public void saveFeedback(FeedbackRequest feedbackRequest) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String dateStr = now.format(DATE_FORMATTER);
            String filename = String.format("feedback_%s.json", dateStr);
            Path filePath = Paths.get(FEEDBACK_DIR, filename);
            
            // Создаем объект с данными обратной связи
            Map<String, Object> feedbackData = new HashMap<>();
            feedbackData.put("timestamp", now.format(DATETIME_FORMATTER));
            feedbackData.put("name", feedbackRequest.getName());
            feedbackData.put("email", feedbackRequest.getEmail());
            feedbackData.put("subject", feedbackRequest.getSubject());
            feedbackData.put("message", feedbackRequest.getMessage());
            
            // Читаем существующие данные или создаем новый список
            List<Map<String, Object>> feedbackList = new ArrayList<>();
            if (Files.exists(filePath)) {
                try {
                    String content = Files.readString(filePath);
                    if (!content.trim().isEmpty()) {
                        feedbackList = objectMapper.readValue(content, 
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                    }
                } catch (Exception e) {
                    log.warn("Ошибка при чтении файла обратной связи: {}", e.getMessage());
                    // Если файл поврежден, создаем новый список
                    feedbackList = new ArrayList<>();
                }
            }
            
            // Добавляем новую запись
            feedbackList.add(feedbackData);
            
            // Сохраняем в файл
            String jsonContent = objectMapper.writeValueAsString(feedbackList);
            Files.writeString(filePath, jsonContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            log.info("Обратная связь сохранена: {} от {} ({})", 
                feedbackRequest.getSubject(), 
                feedbackRequest.getName(), 
                feedbackRequest.getEmail());
                
        } catch (IOException e) {
            log.error("Ошибка при сохранении обратной связи: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось сохранить обратную связь", e);
        }
    }
    
    @Override
    public List<Map<String, Object>> getAllFeedbacks() {
        List<Map<String, Object>> allFeedbacks = new ArrayList<>();
        
        try {
            Path dirPath = Paths.get(FEEDBACK_DIR);
            
            // Если папка не существует, возвращаем пустой список
            if (!Files.exists(dirPath)) {
                log.info("Папка обратной связи не существует: {}", FEEDBACK_DIR);
                return allFeedbacks;
            }
            
            // Читаем все JSON файлы из папки feedback
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "feedback_*.json")) {
                for (Path filePath : stream) {
                    try {
                        String content = Files.readString(filePath);
                        if (!content.trim().isEmpty()) {
                            List<Map<String, Object>> fileFeedbacks = objectMapper.readValue(content,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                            allFeedbacks.addAll(fileFeedbacks);
                        }
                    } catch (Exception e) {
                        log.warn("Ошибка при чтении файла обратной связи {}: {}", filePath.getFileName(), e.getMessage());
                    }
                }
            }
            
            // Сортируем по дате (от новых к старым)
            allFeedbacks.sort((a, b) -> {
                String timestampA = (String) a.get("timestamp");
                String timestampB = (String) b.get("timestamp");
                if (timestampA == null || timestampB == null) {
                    return 0;
                }
                return timestampB.compareTo(timestampA); // Обратный порядок (новые первыми)
            });
            
            log.info("Загружено {} сообщений обратной связи", allFeedbacks.size());
            
        } catch (IOException e) {
            log.error("Ошибка при чтении обратной связи: {}", e.getMessage(), e);
        }
        
        return allFeedbacks;
    }
    
    private void createFeedbackDirectory() {
        try {
            Path dirPath = Paths.get(FEEDBACK_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.info("Создана папка для обратной связи: {}", FEEDBACK_DIR);
            }
        } catch (IOException e) {
            log.error("Ошибка при создании папки для обратной связи: {}", e.getMessage(), e);
        }
    }
}

