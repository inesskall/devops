package dev.yerassyl.aliyev.controller;

import dev.yerassyl.aliyev.dto.LoginRequest;
import dev.yerassyl.aliyev.dto.RegisterRequest;
import dev.yerassyl.aliyev.dto.SuccessEntity;
import dev.yerassyl.aliyev.dto.UserResponse;
import dev.yerassyl.aliyev.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    /**
     * Регистрация нового пользователя
     */
    @PostMapping(value = "/register", produces = "application/json")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegisterRequest request, HttpSession session) {
        log.info("Register request for studentId: {}", request.getStudentId());
        UserResponse userResponse = userService.register(request);
        
        // Сохраняем пользователя в сессию
        session.setAttribute("userId", userResponse.getId());
        session.setAttribute("user", userResponse);
        
        log.info("User registered and session created for userId: {}", userResponse.getId());
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Вход в систему
     */
    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<UserResponse> login(@RequestBody @Valid LoginRequest request, HttpSession session) {
        log.info("Login request for studentId: {}", request.getStudentId());
        UserResponse userResponse = userService.login(request);
        
        // Сохраняем пользователя в сессию
        session.setAttribute("userId", userResponse.getId());
        session.setAttribute("user", userResponse);
        
        log.info("User logged in and session created for userId: {}", userResponse.getId());
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Получить текущего пользователя из сессии
     */
    @GetMapping(value = "/user/current", produces = "application/json")
    public ResponseEntity<UserResponse> getCurrentUser(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            log.warn("No user in session");
            return ResponseEntity.ok(null);
        }
        
        UserResponse userResponse = userService.getCurrentUser(userId);
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Выход из системы
     */
    @PostMapping(value = "/logout", produces = "application/json")
    public ResponseEntity<SuccessEntity> logout(HttpSession session) {
        log.info("Logout request");
        session.invalidate();
        
        SuccessEntity successEntity = new SuccessEntity();
        successEntity.setSuccess(true);
        return ResponseEntity.ok(successEntity);
    }
}

