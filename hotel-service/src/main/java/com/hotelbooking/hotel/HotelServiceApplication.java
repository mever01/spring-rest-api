package com.hotelbooking.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Hotel Service - микросервис для управления отелями и номерами
 * Предоставляет CRUD операции и статистику загрузки номеров
 */
@SpringBootApplication
@EnableDiscoveryClient
public class HotelServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelServiceApplication.class, args);
    }
}


