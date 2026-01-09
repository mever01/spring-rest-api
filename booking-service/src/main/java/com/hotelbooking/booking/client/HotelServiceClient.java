package com.hotelbooking.booking.client;

import com.hotelbooking.booking.dto.RoomDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * Feign клиент для взаимодействия с Hotel Service
 */
@FeignClient(name = "hotel-service")
public interface HotelServiceClient {

    /**
     * Получить рекомендованные номера (с сортировкой по times_booked)
     */
    @GetMapping("/api/rooms/recommend")
    List<RoomDto> getRecommendedRooms();

    /**
     * Подтвердить доступность номера
     */
    @PostMapping("/api/rooms/{id}/confirm-availability")
    boolean confirmRoomAvailability(@PathVariable Long id);

    /**
     * Снять блокировку номера (компенсирующее действие)
     */
    @PostMapping("/api/rooms/{id}/release")
    void releaseRoomBlock(@PathVariable Long id);
}

