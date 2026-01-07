package com.hotelbooking.hotel.mapper;

import com.hotelbooking.hotel.dto.RoomDto;
import com.hotelbooking.hotel.entity.Room;
import org.mapstruct.Mapper;

/**
 * Маппер для преобразования Room сущностей в DTO и обратно
 */
@Mapper(componentModel = "spring")
public interface RoomMapper {

    RoomDto toDto(Room room);

    Room toEntity(RoomDto roomDto);
}
