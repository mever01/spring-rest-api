package com.hotelbooking.booking.mapper;

import com.hotelbooking.booking.dto.UserDto;
import com.hotelbooking.booking.entity.User;
import org.mapstruct.Mapper;

/**
 * Маппер для преобразования User сущностей в DTO и обратно
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto userDto);
}
