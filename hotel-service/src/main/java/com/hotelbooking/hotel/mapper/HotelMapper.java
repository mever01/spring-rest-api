package com.hotelbooking.hotel.mapper;

import com.hotelbooking.hotel.dto.HotelDto;
import com.hotelbooking.hotel.entity.Hotel;
import org.mapstruct.Mapper;

/**
 * Маппер для преобразования Hotel сущностей в DTO и обратно
 */
@Mapper(componentModel = "spring")
public interface HotelMapper {

    HotelDto toDto(Hotel hotel);

    Hotel toEntity(HotelDto hotelDto);
}


