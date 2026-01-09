# Booking Service

Микросервис для управления бронированиями и аутентификацией пользователей.

## Описание

Booking Service предоставляет полную функциональность для работы с бронированиями, включая регистрацию пользователей, аутентификацию и реализацию паттерна Saga для распределенных транзакций.

## Функциональность

### Аутентификация и пользователи

- **Регистрация пользователей** с автоматической выдачей JWT токенов
- **Аутентификация** с валидацией учетных данных
- **Ролевая модель**: USER и ADMIN роли
- **Безопасное хранение** паролей (BCrypt)

### Управление бронированиями

- **Создание бронирований** с автоподбором номера или ручным выбором
- **Просмотр бронирований** пользователя (только свои)
- **Отмена бронирований** с компенсацией
- **Идемпотентность** запросов (защита от дублирования)

### Паттерн Saga

Реализован распределенный паттерн Saga для надежных транзакций:

1. **Создание PENDING** бронирования в локальной БД
2. **Подтверждение доступности** через Hotel Service
3. **Подтверждение/COMPENSATION**:
   - При успехе: PENDING → CONFIRMED
   - При ошибке: PENDING → CANCELLED + компенсация

## API Эндпоинты

### Аутентификация

| Метод | Эндпоинт | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/user/register` | Регистрация пользователя | Открытый |
| POST | `/user/auth` | Аутентификация | Открытый |
| PATCH | `/user` | Обновить пользователя | ADMIN |
| DELETE | `/user` | Удалить пользователя | ADMIN |

### Бронирования

| Метод | Эндпоинт | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/booking` | Создать бронирование | USER |
| GET | `/bookings` | Мои бронирования | USER |
| GET | `/booking/{id}` | Детали бронирования | USER |
| DELETE | `/booking/{id}` | Отменить бронирование | USER |

## Технологии

- **Spring Boot 3.4.7**
- **Spring Data JPA + H2 Database**
- **Spring Security + OAuth2 Resource Server**
- **Spring Cloud Netflix Eureka Client**
- **Spring Cloud OpenFeign** для межсервисного взаимодействия
- **Resilience4j** для отказоустойчивости
- **JWT (io.jsonwebtoken)**
- **MapStruct** для маппинга DTO
- **Java 17**

## Запуск

### Предварительные требования

- Java 17 или выше
- Maven 3.6+
- Запущенные **Eureka Server**, **API Gateway** и **Hotel Service**

### Локальный запуск

```bash
# Перейти в директорию сервиса
cd booking-service

# Собрать проект
mvn clean compile

# Запустить сервис
mvn spring-boot:run
```

Сервис будет доступен по адресу: `http://localhost:8082`

### Docker запуск (опционально)

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/booking-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app.jar"]
```

## Конфигурация

Основные настройки в `application.yml`:

```yaml
server:
  port: 8082

spring:
  application:
    name: booking-service
  datasource:
    url: jdbc:h2:mem:bookingdb
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

jwt:
  secret: ${JWT_SECRET:mySuperSecretKeyForJWTTokenSigningInHotelBookingSystem2024}

resilience4j:
  retry:
    instances:
      confirmAvailability:
        max-attempts: 3
        wait-duration: 500ms
```

## База данных

### Структура

**Таблица users:**
- `id` - первичный ключ
- `username` - уникальное имя пользователя
- `password` - хэшированный пароль
- `role` - роль пользователя (USER/ADMIN)
- `created_at` - дата создания

**Таблица bookings:**
- `id` - первичный ключ
- `user_id` - ссылка на пользователя
- `room_id` - ссылка на номер
- `start_date` - дата заезда
- `end_date` - дата выезда
- `status` - статус бронирования (PENDING/CONFIRMED/CANCELLED)
- `request_id` - ID запроса для идемпотентности
- `created_at` - дата создания

### Предзаполнение данных

При запуске сервис автоматически создает тестовых пользователей:
- **admin** / **admin123** (роль ADMIN)
- **user** / **user123** (роль USER)

## Мониторинг

- **Health Check**: `http://localhost:8082/actuator/health`
- **H2 Console**: `http://localhost:8082/h2-console` (для разработки)
- **Metrics**: `http://localhost:8082/actuator/metrics`
- **Swagger**: `http://localhost:8082/swagger-ui.html`

## Безопасность

### JWT Аутентификация

- **Access Tokens** с временем жизни 1 час
- **HS256 алгоритм** подписи
- **Payload** содержит: username, role, iat, exp

### Ролевая авторизация

- **USER**: управление своими бронированиями, чтение данных
- **ADMIN**: управление пользователями и всеми данными

### Resource Server

Сервис валидирует JWT токены на всех защищенных эндпоинтах.

## Интеграция с другими сервисами

### Hotel Service

- **Рекомендованные номера**: получение отсортированного списка номеров
- **Подтверждение доступности**: блокировка номера для бронирования
- **Компенсация**: снятие блокировки при откате

### API Gateway

- **Маршрутизация**: все запросы проходят через Gateway
- **JWT валидация**: первичная проверка токенов

### Eureka Server

- **Регистрация сервиса**: автоматическая регистрация при запуске
- **Service Discovery**: обнаружение других сервисов

## Алгоритм бронирования

### Процесс создания бронирования

1. **Валидация токена** и извлечение пользователя
2. **Проверка идемпотентности** по requestId
3. **Выбор номера**:
   - Если указан roomId - используем его
   - Если autoSelect=true - получаем рекомендованные номера от Hotel Service
4. **Создание PENDING** бронирования в БД
5. **Подтверждение доступности** через Hotel Service (с retry)
6. **Финализация**:
   - При успехе: статус CONFIRMED, инкремент times_booked
   - При ошибке: статус CANCELLED, компенсация

### Обработка ошибок

- **Retry механизм**: до 3 попыток подтверждения доступности
- **Компенсация**: автоматический откат при ошибках
- **Идемпотентность**: защита от дублирования запросов

## Разработка

### Структура проекта

```
booking-service/
├── src/main/java/com/hotelbooking/booking/
│   ├── BookingServiceApplication.java
│   ├── controller/
│   │   ├── UserController.java
│   │   └── BookingController.java
│   ├── service/
│   │   ├── UserService.java
│   │   └── BookingService.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── BookingRepository.java
│   ├── entity/
│   │   ├── User.java
│   │   └── Booking.java
│   ├── dto/
│   │   ├── AuthRequest.java
│   │   ├── AuthResponse.java
│   │   ├── RegisterRequest.java
│   │   ├── UserDto.java
│   │   ├── BookingDto.java
│   │   ├── CreateBookingRequest.java
│   │   └── RoomDto.java
│   ├── mapper/
│   │   ├── UserMapper.java
│   │   └── BookingMapper.java
│   ├── client/
│   │   └── HotelServiceClient.java
│   ├── config/
│   │   ├── DataInitializer.java
│   │   ├── SecurityConfig.java
│   │   └── ResilienceConfig.java
│   └── exception/
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   └── application.yml
├── src/test/java/
│   └── com/hotelbooking/booking/service/
│       └── UserServiceTest.java
├── pom.xml
└── README.md
```

### Сборка и тестирование

```bash
# Сборка
mvn clean package

# Запуск тестов
mvn test

# Запуск с профилем
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Тестирование API

```bash
# Регистрация
curl -X POST http://localhost:8082/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Аутентификация
curl -X POST http://localhost:8082/user/auth \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'

# Создание бронирования (с токеном)
curl -X POST http://localhost:8082/booking \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{"startDate":"2024-12-25","endDate":"2024-12-27","autoSelect":true}'
```

## Resilience и отказоустойчивость

### Retry Configuration

```yaml
resilience4j:
  retry:
    instances:
      confirmAvailability:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: false
```

### Circuit Breaker (расширение)

Можно добавить Circuit Breaker для защиты от cascade failures:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      hotelService:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
```

