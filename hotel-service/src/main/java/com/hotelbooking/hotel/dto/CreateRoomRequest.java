package com.hotelbooking.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO для создания номера
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {

    @NotBlank(message = "Номер комнаты обязателен")
    private String number;

    @NotNull(message = "ID отеля обязателен")
    private Long hotelId;
}

