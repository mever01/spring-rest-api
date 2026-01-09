# Eureka Server

Сервис обнаружения микросервисов для системы бронирования отелей.

## Описание

Eureka Server является центральным реестром всех микросервисов в системе. Он предоставляет функциональность Service Discovery, позволяя сервисам автоматически находить друг друга и взаимодействовать.

## Функциональность

- **Регистрация сервисов**: Автоматическая регистрация микросервисов при их запуске
- **Обнаружение сервисов**: Предоставление информации о доступных сервисах другим компонентам
- **Health Check**: Мониторинг состояния зарегистрированных сервисов
- **Load Balancing**: Поддержка балансировки нагрузки через интеграцию с Ribbon

## Технологии

- **Spring Boot 3.4.7**
- **Spring Cloud Netflix Eureka Server**
- **Java 17**

## Запуск

### Предварительные требования

- Java 17 или выше
- Maven 3.6+

### Локальный запуск

```bash
# Перейти в директорию сервиса
cd eureka-server

# Собрать проект
mvn clean compile

# Запустить сервис
mvn spring-boot:run
```

Сервис будет доступен по адресу: `http://localhost:8761`

### Docker запуск (опционально)

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/eureka-server-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## Конфигурация

Основные настройки в `application.yml`:

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  client:
    register-with-eureka: false  # Не регистрируется сам у себя
    fetch-registry: false        # Не получает реестр
  server:
    enable-self-preservation: false  # Отключено для разработки
```

## Мониторинг

- **Eureka Dashboard**: `http://localhost:8761` - веб-интерфейс для просмотра зарегистрированных сервисов
- **Health Check**: `http://localhost:8761/actuator/health` - проверка состояния сервера

## Архитектурная роль

Eureka Server является фундаментом микросервисной архитектуры:

1. **Первый сервис для запуска** - должен быть запущен перед всеми остальными сервисами
2. **Центральный реестр** - хранит информацию о всех доступных микросервисах
3. **Высокая доступность** - может быть развернут в кластере для отказоустойчивости

## Зависимости

Другие сервисы зависят от Eureka Server:
- **API Gateway** - использует для маршрутизации запросов
- **Hotel Service** - регистрируется для предоставления API
- **Booking Service** - регистрируется для предоставления API

## Разработка

### Структура проекта

```
eureka-server/
├── src/main/java/com/hotelbooking/eureka/
│   └── EurekaServerApplication.java
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

Запустите класс `EurekaServerApplication.java` как Spring Boot приложение.

