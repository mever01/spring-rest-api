package com.hotelbooking.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для номера (упрощенный для клиента)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {

    private Long id;
    private String number;
    private Long hotelId;
    private Boolean available;
    private Integer timesBooked;
}

