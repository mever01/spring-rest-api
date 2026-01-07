package com.hotelbooking.booking.service;

import com.hotelbooking.booking.dto.RegisterRequest;
import com.hotelbooking.booking.entity.User;
import com.hotelbooking.booking.mapper.UserMapper;
import com.hotelbooking.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Тесты для UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMapper, passwordEncoder, "testSecretKeyForJWTTokenSigningWithMinimum256BitsRequiredForSecurity");
    }

    @Test
    void register_ShouldCreateNewUser() {
        // Given
        RegisterRequest request = new RegisterRequest("testuser", "password123");
        User savedUser = new User(1L, "testuser", "encodedPassword", User.Role.USER, null);

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        var response = userService.register(request);

        // Then
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getToken()).isNotNull();
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        // Given
        RegisterRequest request = new RegisterRequest("existinguser", "password123");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Пользователь с таким именем уже существует");
    }

    @Test
    void authenticate_ShouldReturnToken_WhenCredentialsValid() {
        // Given
        User user = new User(1L, "testuser", "encodedPassword", User.Role.USER, null);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // When
        var response = userService.authenticate(
                new com.hotelbooking.booking.dto.AuthRequest("testuser", "password123"));

        // Then
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getToken()).isNotNull();
    }

    @Test
    void authenticate_ShouldThrowException_WhenPasswordInvalid() {
        // Given
        User user = new User(null, "testuser", "encodedPassword", User.Role.USER, null);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.authenticate(
                new com.hotelbooking.booking.dto.AuthRequest("testuser", "wrongpassword")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Неверное имя пользователя или пароль");
    }
}
