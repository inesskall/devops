package dev.yerassyl.aliyev.serviceImp;

import dev.yerassyl.aliyev.constants.ErrorMessages;
import dev.yerassyl.aliyev.dto.LoginRequest;
import dev.yerassyl.aliyev.dto.RegisterRequest;
import dev.yerassyl.aliyev.dto.UserResponse;
import dev.yerassyl.aliyev.entity.User;
import dev.yerassyl.aliyev.exception.InvalidRequestException;
import dev.yerassyl.aliyev.repository.UserRepository;
import dev.yerassyl.aliyev.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse register(RegisterRequest request) {
        // Проверяем, не существует ли уже пользователь с таким studentId
        if (userRepository.existsByStudentId(request.getStudentId())) {
            log.error("User with studentId {} already exists", request.getStudentId());
            throw new InvalidRequestException("Пользователь с таким ID уже зарегистрирован");
        }

        // Создаем нового пользователя
        User user = User.builder()
                .studentId(request.getStudentId())
                .name(request.getName())
                .surname(request.getSurname())
                .password(hashPassword(request.getPassword()))
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully with studentId: {}", user.getStudentId());

        return UserResponse.builder()
                .id(user.getId())
                .studentId(user.getStudentId())
                .name(user.getName())
                .surname(user.getSurname())
                .build();
    }

    @Override
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByStudentId(request.getStudentId())
                .orElseThrow(() -> {
                    log.error("User with studentId {} not found", request.getStudentId());
                    return new InvalidRequestException("Неверный ID студента или пароль");
                });

        // Проверяем пароль
        String hashedPassword = hashPassword(request.getPassword());
        if (!user.getPassword().equals(hashedPassword)) {
            log.error("Invalid password for user with studentId: {}", request.getStudentId());
            throw new InvalidRequestException("Неверный ID студента или пароль");
        }

        log.info("User logged in successfully with studentId: {}", user.getStudentId());

        return UserResponse.builder()
                .id(user.getId())
                .studentId(user.getStudentId())
                .name(user.getName())
                .surname(user.getSurname())
                .build();
    }

    @Override
    public UserResponse getCurrentUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", userId);
                    return new InvalidRequestException(ErrorMessages.INVALID_ID_EXISTENCE);
                });

        return UserResponse.builder()
                .id(user.getId())
                .studentId(user.getStudentId())
                .name(user.getName())
                .surname(user.getSurname())
                .build();
    }

    /**
     * Хеширует пароль используя SHA-256
     * В продакшене лучше использовать BCrypt
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Error hashing password", e);
            throw new RuntimeException("Ошибка при обработке пароля");
        }
    }
}

