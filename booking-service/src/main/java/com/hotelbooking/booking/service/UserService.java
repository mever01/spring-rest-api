package com.hotelbooking.booking.service;

import com.hotelbooking.booking.dto.AuthRequest;
import com.hotelbooking.booking.dto.AuthResponse;
import com.hotelbooking.booking.dto.RegisterRequest;
import com.hotelbooking.booking.dto.UserDto;
import com.hotelbooking.booking.entity.User;
import com.hotelbooking.booking.mapper.UserMapper;
import com.hotelbooking.booking.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Сервис для управления пользователями и аутентификацией
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${jwt.secret:mySuperSecretKeyForJWTTokenSigningInHotelBookingSystem2024}")
    private String jwtSecretString;
    private final Duration jwtExpiration = Duration.ofHours(1); // 1 час

    private SecretKey getJwtSecret() {
        return Keys.hmacShaKeyFor((jwtSecretString != null ? jwtSecretString : "testSecretKeyForJWTTokenSigningWithMinimum256BitsRequiredForSecurity").getBytes());
    }

    // Конструктор для Spring (параметры будут инжектированы)
    @org.springframework.beans.factory.annotation.Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // Конструктор для тестов
    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, String jwtSecret) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtSecretString = jwtSecret;
    }

    /**
     * Регистрация нового пользователя
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER); // По умолчанию USER

        user = userRepository.save(user);
        return generateAuthResponse(user);
    }

    /**
     * Аутентификация пользователя
     */
    public AuthResponse authenticate(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Неверное имя пользователя или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Неверное имя пользователя или пароль");
        }

        return generateAuthResponse(user);
    }

    /**
     * Получить пользователя по имени
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    /**
     * Получить пользователя по ID
     */
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        UserDto userDto = userMapper.toDto(user);
        userDto.setRole(user.getRole().name()); // Устанавливаем роль вручную
        return userDto;
    }

    /**
     * Создать пользователя (только для админов)
     */
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setRole(User.Role.valueOf(userDto.getRole()));
        user.setPassword(passwordEncoder.encode("defaultPassword123")); // Временный пароль
        user = userRepository.save(user);

        UserDto result = userMapper.toDto(user);
        result.setRole(user.getRole().name());
        return result;
    }

    /**
     * Обновить пользователя
     */
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setUsername(userDto.getUsername());
        if (userDto.getRole() != null) {
            user.setRole(User.Role.valueOf(userDto.getRole()));
        }

        user = userRepository.save(user);
        UserDto result = userMapper.toDto(user);
        result.setRole(user.getRole().name());
        return result;
    }

    /**
     * Удалить пользователя
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Пользователь не найден");
        }
        userRepository.deleteById(id);
    }

    /**
     * Генерация JWT токена
     */
    private AuthResponse generateAuthResponse(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtExpiration);

        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(getJwtSecret(), SignatureAlgorithm.HS256)
                .compact();

        return new AuthResponse(token, "Bearer", user.getUsername(), user.getRole().name());
    }
}
