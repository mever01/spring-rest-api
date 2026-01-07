package com.hotelbooking.booking.config;

import com.hotelbooking.booking.entity.User;
import com.hotelbooking.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Инициализация тестовых данных после запуска приложения
 */
@Component
@RequiredArgsConstructor
@Profile("!test") // Не выполнять в тестах
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            initializeData();
        }
    }

    private void initializeData() {
        // Создание пользователей (пароли захэшированы BCrypt)
        // Пароль для admin: admin123
        // Пароль для user: user123

        User admin = new User(null, "admin", passwordEncoder.encode("admin123"), User.Role.ADMIN, null);
        userRepository.save(admin);

        User user = new User(null, "user", passwordEncoder.encode("user123"), User.Role.USER, null);
        userRepository.save(user);
    }
}
