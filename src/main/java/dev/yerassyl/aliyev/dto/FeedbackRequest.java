package dev.yerassyl.aliyev.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса обратной связи от пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {
    
    @NotBlank(message = "Имя обязательно для заполнения")
    @Size(min = 2, max = 100, message = "Имя должно быть от 2 до 100 символов")
    private String name;
    
    @NotBlank(message = "Email обязателен для заполнения")
    @Email(message = "Некорректный формат email")
    private String email;
    
    @NotBlank(message = "Тема сообщения обязательна для заполнения")
    @Size(min = 3, max = 200, message = "Тема должна быть от 3 до 200 символов")
    private String subject;
    
    @NotBlank(message = "Сообщение обязательно для заполнения")
    @Size(min = 10, max = 2000, message = "Сообщение должно быть от 10 до 2000 символов")
    private String message;
}

