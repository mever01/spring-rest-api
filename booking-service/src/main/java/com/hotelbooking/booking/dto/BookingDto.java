package com.hotelbooking.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO для бронирования
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private Long id;

    private Long userId;

    private String username;

    private Long roomId;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private LocalDateTime createdAt;
}
