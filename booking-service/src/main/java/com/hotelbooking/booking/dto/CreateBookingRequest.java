package com.hotelbooking.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO для создания бронирования
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    private Long roomId;

    @NotNull(message = "Дата заезда обязательна")
    @FutureOrPresent(message = "Дата заезда не может быть в прошлом")
    private LocalDate startDate;

    @NotNull(message = "Дата выезда обязательна")
    @Future(message = "Дата выезда должна быть в будущем")
    private LocalDate endDate;

    private Boolean autoSelect = false; // Автоподбор номера

    private String requestId; // Для идемпотентности
}
