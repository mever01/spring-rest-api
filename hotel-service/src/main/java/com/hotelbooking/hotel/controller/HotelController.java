package com.hotelbooking.hotel.controller;

import com.hotelbooking.hotel.dto.HotelDto;
import com.hotelbooking.hotel.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST контроллер для управления отелями
 */
@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotel Management", description = "API для управления отелями")
public class HotelController {

    private final HotelService hotelService;

    /**
     * Получить все отели
     */
    @GetMapping
    @Operation(summary = "Получить список всех отелей")
    public ResponseEntity<List<HotelDto>> getAllHotels() {
        List<HotelDto> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(hotels);
    }

    /**
     * Получить отель по ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить отель по ID")
    public ResponseEntity<HotelDto> getHotel(@PathVariable Long id) {
        HotelDto hotel = hotelService.getHotelById(id);
        return ResponseEntity.ok(hotel);
    }

    /**
     * Создать отель (только ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новый отель")
    public ResponseEntity<HotelDto> createHotel(@Valid @RequestBody HotelDto hotelDto) {
        HotelDto createdHotel = hotelService.createHotel(hotelDto);
        return ResponseEntity.ok(createdHotel);
    }

    /**
     * Обновить отель (только ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить отель")
    public ResponseEntity<HotelDto> updateHotel(@PathVariable Long id, @Valid @RequestBody HotelDto hotelDto) {
        HotelDto updatedHotel = hotelService.updateHotel(id, hotelDto);
        return ResponseEntity.ok(updatedHotel);
    }

    /**
     * Удалить отель (только ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить отель")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }
}

