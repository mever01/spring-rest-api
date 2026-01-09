package com.hotelbooking.hotel.controller;

import com.hotelbooking.hotel.dto.CreateRoomRequest;
import com.hotelbooking.hotel.dto.RoomDto;
import com.hotelbooking.hotel.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST контроллер для управления номерами
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Room Management", description = "API для управления номерами")
public class RoomController {

    private final RoomService roomService;

    /**
     * Получить все доступные номера (без специальной сортировки)
     */
    @GetMapping
    @Operation(summary = "Получить список всех доступных номеров")
    public ResponseEntity<List<RoomDto>> getAllAvailableRooms() {
        List<RoomDto> rooms = roomService.getAllAvailableRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * Получить рекомендованные номера (сортировка по times_booked для равномерной загрузки)
     */
    @GetMapping("/recommend")
    @Operation(summary = "Получить рекомендованные номера с равномерной загрузкой")
    public ResponseEntity<List<RoomDto>> getRecommendedRooms() {
        List<RoomDto> rooms = roomService.getRecommendedRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * Получить номера по отелю
     */
    @GetMapping("/hotel/{hotelId}")
    @Operation(summary = "Получить номера по ID отеля")
    public ResponseEntity<List<RoomDto>> getRoomsByHotel(@PathVariable Long hotelId) {
        List<RoomDto> rooms = roomService.getRoomsByHotel(hotelId);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Создать номер (только ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новый номер")
    public ResponseEntity<RoomDto> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        RoomDto createdRoom = roomService.createRoom(request);
        return ResponseEntity.ok(createdRoom);
    }

    /**
     * Подтвердить доступность номера (для внутреннего использования сагами)
     */
    @PostMapping("/{id}/confirm-availability")
    @Operation(summary = "Подтвердить доступность номера")
    public ResponseEntity<Boolean> confirmRoomAvailability(@PathVariable Long id) {
        boolean available = roomService.confirmRoomAvailability(id);
        return ResponseEntity.ok(available);
    }

    /**
     * Снять блокировку номера (компенсирующее действие для саги)
     */
    @PostMapping("/{id}/release")
    @Operation(summary = "Снять блокировку номера")
    public ResponseEntity<Void> releaseRoomBlock(@PathVariable Long id) {
        roomService.releaseRoomBlock(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Получить статистику номеров (только ADMIN)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить статистику загрузки номеров")
    public ResponseEntity<List<RoomDto>> getRoomStatistics() {
        List<RoomDto> statistics = roomService.getRoomStatistics();
        return ResponseEntity.ok(statistics);
    }
}


