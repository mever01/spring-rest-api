package com.hotelbooking.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа аутентификации
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    private String type = "Bearer";

    private String username;

    private String role;
}

