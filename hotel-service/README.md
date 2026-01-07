# Hotel Service

Микросервис для управления отелями и номерами в системе бронирования.

## Описание

Hotel Service предоставляет полную функциональность для работы с отелями и номерами: создание, чтение, обновление и удаление. Включает интеллектуальный алгоритм планирования занятости номеров для равномерной загрузки.

## Функциональность

### Управление отелями

- **Создание отелей** (только ADMIN)
- **Просмотр списка отелей** (все пользователи)
- **Получение деталей отеля** по ID
- **Обновление информации** об отеле (только ADMIN)
- **Удаление отелей** (только ADMIN)

### Управление номерами

- **Создание номеров** в отелях (только ADMIN)
- **Просмотр доступных номеров** (все пользователи)
- **Рекомендованные номера** с сортировкой по загрузке
- **Проверка доступности** номера для бронирования

### Алгоритм планирования

Система использует интеллектуальный алгоритм распределения номеров:

1. **Сортировка по статистике**: номера сортируются по возрастанию `times_booked`
2. **Равномерная загрузка**: система стремится к равномерному распределению нагрузки
3. **Автоподбор**: Booking Service может автоматически выбирать оптимальный номер

## API Эндпоинты

### Отели

| Метод | Эндпоинт | Описание | Доступ |
|-------|----------|----------|--------|
| GET | `/api/hotels` | Получить все отели | Все |
| GET | `/api/hotels/{id}` | Получить отель по ID | Все |
| POST | `/api/hotels` | Создать отель | ADMIN |
| PUT | `/api/hotels/{id}` | Обновить отель | ADMIN |
| DELETE | `/api/hotels/{id}` | Удалить отель | ADMIN |

### Номера

| Метод | Эндпоинт | Описание | Доступ |
|-------|----------|----------|--------|
| GET | `/api/rooms` | Получить доступные номера | Все |
| GET | `/api/rooms/recommend` | Рекомендованные номера | Все |
| GET | `/api/rooms/hotel/{hotelId}` | Номера по отелю | Все |
| POST | `/api/rooms` | Создать номер | ADMIN |
| POST | `/api/rooms/{id}/confirm-availability` | Подтвердить доступность | INTERNAL |
| POST | `/api/rooms/{id}/release` | Снять блокировку | INTERNAL |
| GET | `/api/rooms/statistics` | Статистика номеров | ADMIN |

## Технологии

- **Spring Boot 3.4.7**
- **Spring Data JPA + H2 Database**
- **Spring Security + OAuth2 Resource Server**
- **Spring Cloud Netflix Eureka Client**
- **MapStruct** для маппинга DTO
- **Java 17**

## Запуск

### Предварительные требования

- Java 17 или выше
- Maven 3.6+
- Запущенные **Eureka Server** и **API Gateway**

### Локальный запуск

```bash
# Перейти в директорию сервиса
cd hotel-service

# Собрать проект
mvn clean compile

# Запустить сервис
mvn spring-boot:run
```

Сервис будет доступен по адресу: `http://localhost:8081`

### Docker запуск (опционально)

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/hotel-service-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app.jar"]
```

## Конфигурация

Основные настройки в `application.yml`:

```yaml
server:
  port: 8081

spring:
  application:
    name: hotel-service
  datasource:
    url: jdbc:h2:mem:hoteldb
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
```

## База данных

### Структура

**Таблица hotels:**
- `id` - первичный ключ
- `name` - название отеля
- `address` - адрес отеля

**Таблица rooms:**
- `id` - первичный ключ
- `number` - номер комнаты
- `hotel_id` - ссылка на отель
- `available` - доступность номера
- `times_booked` - количество бронирований

### Предзаполнение данных

При запуске сервис автоматически создает тестовые данные:
- 3 отеля (Гранд Отель Москва, Спорт Отель, Бизнес Центр)
- 8 номеров с различной загрузкой

## Мониторинг

- **Health Check**: `http://localhost:8081/actuator/health`
- **H2 Console**: `http://localhost:8081/h2-console` (для разработки)
- **Metrics**: `http://localhost:8081/actuator/metrics`
- **Swagger**: `http://localhost:8081/swagger-ui.html`

## Безопасность

### Resource Server

Сервис использует OAuth2 Resource Server для валидации JWT токенов:

- **Публичные эндпоинты**: чтение отелей и номеров
- **Защищенные эндпоинты**: создание/обновление/удаление (ADMIN only)
- **Internal эндпоинты**: для Saga паттерна (без дополнительной проверки)

### Ролевая модель

- **USER**: чтение данных
- **ADMIN**: полное управление отелями и номерами

## Интеграция с другими сервисами

### Booking Service

- **Рекомендованные номера**: предоставляет отсортированный список номеров
- **Подтверждение доступности**: временная блокировка для Saga транзакций
- **Компенсация**: снятие блокировки при откате транзакции

### API Gateway

- **Маршрутизация**: все запросы проходят через Gateway
- **JWT передача**: токены автоматически передаются в заголовках

## Разработка

### Структура проекта

```
hotel-service/
├── src/main/java/com/hotelbooking/hotel/
│   ├── HotelServiceApplication.java
│   ├── controller/
│   │   ├── HotelController.java
│   │   └── RoomController.java
│   ├── service/
│   │   ├── HotelService.java
│   │   └── RoomService.java
│   ├── repository/
│   │   ├── HotelRepository.java
│   │   └── RoomRepository.java
│   ├── entity/
│   │   ├── Hotel.java
│   │   └── Room.java
│   ├── dto/
│   │   ├── HotelDto.java
│   │   ├── RoomDto.java
│   │   └── CreateRoomRequest.java
│   ├── mapper/
│   │   ├── HotelMapper.java
│   │   └── RoomMapper.java
│   ├── config/
│   │   ├── DataInitializer.java
│   │   └── SecurityConfig.java
│   └── exception/
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.yml
│   └── schema.sql
├── src/test/java/
│   └── com/hotelbooking/hotel/service/
│       └── HotelServiceTest.java
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
# Получить все отели
curl http://localhost:8081/api/hotels

# Получить рекомендованные номера
curl http://localhost:8081/api/rooms/recommend
```
