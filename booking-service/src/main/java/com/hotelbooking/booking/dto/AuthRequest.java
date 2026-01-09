package com.hotelbooking.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO для аутентификации пользователя
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "Имя пользователя обязательно")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    private String password;
}

