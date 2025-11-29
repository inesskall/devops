package dev.yerassyl.aliyev.service;

import dev.yerassyl.aliyev.dto.LoginRequest;
import dev.yerassyl.aliyev.dto.RegisterRequest;
import dev.yerassyl.aliyev.dto.UserResponse;

public interface UserService {
    UserResponse register(RegisterRequest request);
    UserResponse login(LoginRequest request);
    UserResponse getCurrentUser(Integer userId);
}

