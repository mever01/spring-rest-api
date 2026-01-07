package com.hotelbooking.booking.controller;

import com.hotelbooking.booking.dto.BookingDto;
import com.hotelbooking.booking.dto.CreateBookingRequest;
import com.hotelbooking.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST контроллер для управления бронированиями
 */
@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "API для управления бронированиями")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Создать бронирование
     */
    @PostMapping
    @Operation(summary = "Создать бронирование")
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody CreateBookingRequest request,
                                                   Authentication authentication) {
        String username = authentication.getName();
        BookingDto booking = bookingService.createBooking(request, username);
        return ResponseEntity.ok(booking);
    }

    /**
     * Получить все бронирования пользователя
     */
    @GetMapping("s")
    @Operation(summary = "Получить все бронирования пользователя")
    public ResponseEntity<List<BookingDto>> getUserBookings(Authentication authentication) {
        String username = authentication.getName();
        List<BookingDto> bookings = bookingService.getUserBookings(username);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Получить бронирование по ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить бронирование по ID")
    public ResponseEntity<BookingDto> getBooking(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        BookingDto booking = bookingService.getBookingById(id, username);
        return ResponseEntity.ok(booking);
    }

    /**
     * Отменить бронирование
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Отменить бронирование")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        bookingService.cancelBooking(id, username);
        return ResponseEntity.noContent().build();
    }
}
