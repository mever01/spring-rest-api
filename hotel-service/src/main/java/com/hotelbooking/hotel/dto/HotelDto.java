package com.hotelbooking.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO для отеля
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelDto {

    private Long id;

    @NotBlank(message = "Название отеля обязательно")
    private String name;

    @NotBlank(message = "Адрес отеля обязателен")
    private String address;
}


