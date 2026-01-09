# API Gateway

Единая точка входа для всех клиентских запросов в системе бронирования отелей.

## Описание

API Gateway является шлюзом между клиентами и микросервисами. Он предоставляет единый API, маршрутизацию запросов, аутентификацию JWT токенов и централизованное логирование.

## Функциональность

- **Маршрутизация запросов**: Автоматическая маршрутизация запросов к соответствующим микросервисам
- **JWT Аутентификация**: Проверка и передача JWT токенов в downstream сервисы
- **Service Discovery**: Интеграция с Eureka для динамического обнаружения сервисов
- **Load Balancing**: Распределение нагрузки между экземплярами сервисов
- **Централизованное логирование**: Единая точка для логирования всех запросов

## Маршруты

API Gateway маршрутизирует запросы по следующим правилам:

| Путь | Сервис | Описание |
|------|--------|----------|
| `/api/bookings/**` | Booking Service | Управление бронированиями |
| `/api/booking/**` | Booking Service | Операции с бронированиями |
| `/api/user/**` | Booking Service | Аутентификация и пользователи |
| `/api/hotels/**` | Hotel Service | Управление отелями |
| `/api/rooms/**` | Hotel Service | Управление номерами |

## Технологии

- **Spring Boot 3.4.7**
- **Spring Cloud Gateway**
- **Spring Cloud Netflix Eureka Client**
- **JWT (io.jsonwebtoken)**
- **Java 17**

## Запуск

### Предварительные требования

- Java 17 или выше
- Maven 3.6+
- Запущенный **Eureka Server**

### Локальный запуск

```bash
# Перейти в директорию сервиса
cd api-gateway

# Собрать проект
mvn clean compile

# Запустить сервис
mvn spring-boot:run
```

Сервис будет доступен по адресу: `http://localhost:8080`

### Docker запуск (опционально)

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/api-gateway-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## Конфигурация

Основные настройки в `application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

jwt:
  secret: ${JWT_SECRET:mySuperSecretKeyForJWTTokenSigningInHotelBookingSystem2024}
```

## Мониторинг

- **Health Check**: `http://localhost:8080/actuator/health`
- **Routes Info**: `http://localhost:8080/actuator/routes` - список активных маршрутов
- **Gateway Metrics**: `http://localhost:8080/actuator/metrics` - метрики производительности

## Безопасность

### JWT Аутентификация

API Gateway проверяет JWT токены на всех защищенных эндпоинтах:

1. **Извлечение токена** из заголовка `Authorization: Bearer <token>`
2. **Валидация токена** с использованием секретного ключа
3. **Извлечение информации** о пользователе (username, role)
4. **Передача информации** downstream сервисам через заголовки:
   - `X-User-Username`
   - `X-User-Role`
   - `X-JWT-Token`

### Публичные эндпоинты

Некоторые эндпоинты доступны без аутентификации:
- `/api/user/register` - регистрация пользователей
- `/api/user/auth` - аутентификация
- `/api/hotels` - просмотр отелей
- `/api/rooms` - просмотр номеров
- `/api/rooms/recommend` - рекомендованные номера

## Архитектурная роль

API Gateway играет ключевую роль в микросервисной архитектуре:

1. **Единая точка входа** - все клиентские запросы проходят через Gateway
2. **Абстракция сервисов** - клиенты не знают о внутренней структуре сервисов
3. **Централизованная безопасность** - единая точка для аутентификации и авторизации
4. **Мониторинг и логирование** - централизованный сбор метрик и логов

## Зависимости

- **Eureka Server** - для обнаружения сервисов
- **Hotel Service** - для маршрутизации запросов к отелям
- **Booking Service** - для маршрутизации запросов к бронированиям

## Разработка

### Структура проекта

```
api-gateway/
├── src/main/java/com/hotelbooking/gateway/
│   ├── ApiGatewayApplication.java
│   ├── config/
│   │   ├── GatewayConfig.java          # Конфигурация маршрутизации
│   │   └── JwtAuthenticationFilter.java # JWT фильтр
│   └── exception/
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   └── application.yml
├── pom.xml
└── README.md
```

### Сборка

```bash
mvn clean package
```

### Запуск в IDE

Запустите класс `ApiGatewayApplication.java` как Spring Boot приложение.

