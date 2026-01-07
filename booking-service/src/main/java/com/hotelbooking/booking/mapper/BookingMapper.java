package com.hotelbooking.booking.mapper;

import com.hotelbooking.booking.dto.BookingDto;
import com.hotelbooking.booking.entity.Booking;
import org.mapstruct.Mapper;

/**
 * Маппер для преобразования Booking сущностей в DTO и обратно
 */
@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingDto toDto(Booking booking);

    Booking toEntity(BookingDto bookingDto);
}
