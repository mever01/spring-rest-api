package com.hotelbooking.booking.service;

import com.hotelbooking.booking.client.HotelServiceClient;
import com.hotelbooking.booking.dto.BookingDto;
import com.hotelbooking.booking.dto.CreateBookingRequest;
import com.hotelbooking.booking.dto.RoomDto;
import com.hotelbooking.booking.entity.Booking;
import com.hotelbooking.booking.entity.User;
import com.hotelbooking.booking.mapper.BookingMapper;
import com.hotelbooking.booking.repository.BookingRepository;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис для управления бронированиями с паттерном Saga
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final HotelServiceClient hotelServiceClient;

    /**
     * Создать бронирование с паттерном Saga
     */
    @Transactional
    public BookingDto createBooking(CreateBookingRequest request, String username) {
        log.info("Starting booking creation for user: {}, requestId: {}", username, request.getRequestId());

        User user = userService.getUserByUsername(username);

        // Для идемпотентности - проверяем существующий запрос
        if (request.getRequestId() != null) {
            Booking existingBooking = bookingRepository.findByRequestId(request.getRequestId()).orElse(null);
            if (existingBooking != null) {
                log.info("Found existing booking for requestId: {}", request.getRequestId());
                return bookingMapper.toDto(existingBooking);
            }
        }

        // Генерируем requestId если не указан
        if (request.getRequestId() == null) {
            request.setRequestId(UUID.randomUUID().toString());
        }

        Long roomId = request.getRoomId();

        // Автоподбор номера если не указан
        if (request.getAutoSelect() != null && request.getAutoSelect()) {
            roomId = selectOptimalRoom();
            log.info("Auto-selected room: {} for user: {}", roomId, username);
        }

        if (roomId == null) {
            throw new RuntimeException("Не удалось выбрать номер");
        }

        // Шаг 1: Создаем бронирование в статусе PENDING
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoomId(roomId);
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setStatus(Booking.Status.PENDING);
        booking.setRequestId(request.getRequestId());

        booking = bookingRepository.save(booking);
        log.info("Created booking in PENDING status: {}", booking.getId());

        try {
            // Шаг 2: Вызываем Hotel Service для подтверждения доступности
            boolean available = confirmRoomAvailabilityWithRetry(roomId);

            if (available) {
                // Шаг 3: Подтверждаем бронирование
                booking.setStatus(Booking.Status.CONFIRMED);
                booking = bookingRepository.save(booking);
                log.info("Booking confirmed: {}", booking.getId());

                return bookingMapper.toDto(booking);
            } else {
                // Компенсация: отменяем бронирование
                performCompensation(booking);
                throw new RuntimeException("Номер недоступен на выбранные даты");
            }

        } catch (Exception e) {
            log.error("Error during booking confirmation: {}", e.getMessage());
            // Компенсация при ошибке
            performCompensation(booking);
            throw new RuntimeException("Ошибка при подтверждении бронирования: " + e.getMessage());
        }
    }

    /**
     * Подтверждение доступности номера с retry
     */
    @Retry(name = "confirmAvailability", fallbackMethod = "confirmAvailabilityFallback")
    private boolean confirmRoomAvailabilityWithRetry(Long roomId) {
        log.debug("Confirming availability for room: {}", roomId);
        return hotelServiceClient.confirmRoomAvailability(roomId);
    }

    /**
     * Fallback для retry - возвращаем false при неудаче
     */
    private boolean confirmAvailabilityFallback(Long roomId, Exception e) {
        log.warn("Failed to confirm availability for room: {}, error: {}", roomId, e.getMessage());
        return false;
    }

    /**
     * Выбор оптимального номера (алгоритм планирования)
     */
    private Long selectOptimalRoom() {
        try {
            List<RoomDto> recommendedRooms = hotelServiceClient.getRecommendedRooms();
            if (!recommendedRooms.isEmpty()) {
                // Берем первый рекомендованный номер (уже отсортирован по times_booked)
                return recommendedRooms.get(0).getId();
            }
        } catch (Exception e) {
            log.error("Error selecting optimal room: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Компенсирующее действие: отменяем бронирование и снимаем блокировку
     */
    private void performCompensation(Booking booking) {
        log.info("Performing compensation for booking: {}", booking.getId());

        try {
            // Отменяем бронирование
            booking.setStatus(Booking.Status.CANCELLED);
            bookingRepository.save(booking);

            // Снимаем блокировку номера
            hotelServiceClient.releaseRoomBlock(booking.getRoomId());

            log.info("Compensation completed for booking: {}", booking.getId());
        } catch (Exception e) {
            log.error("Error during compensation for booking: {}", booking.getId(), e);
        }
    }

    /**
     * Получить все бронирования пользователя
     */
    public List<BookingDto> getUserBookings(String username) {
        User user = userService.getUserByUsername(username);
        return bookingRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить бронирование по ID
     */
    public BookingDto getBookingById(Long id, String username) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

        // Проверяем, что пользователь имеет доступ только к своим бронированиям
        if (!booking.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Доступ запрещен");
        }

        return convertToDto(booking);
    }

    /**
     * Отменить бронирование
     */
    @Transactional
    public void cancelBooking(Long id, String username) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

        if (!booking.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Доступ запрещен");
        }

        if (booking.getStatus() == Booking.Status.CANCELLED) {
            throw new RuntimeException("Бронирование уже отменено");
        }

        booking.setStatus(Booking.Status.CANCELLED);
        bookingRepository.save(booking);

        // Освобождаем номер
        try {
            hotelServiceClient.releaseRoomBlock(booking.getRoomId());
        } catch (Exception e) {
            log.warn("Could not release room block for cancelled booking: {}", id);
        }
    }

    /**
     * Преобразование Booking в DTO с правильным маппингом
     */
    private BookingDto convertToDto(Booking booking) {
        BookingDto dto = bookingMapper.toDto(booking);
        dto.setUserId(booking.getUser().getId());
        dto.setUsername(booking.getUser().getUsername());
        dto.setStatus(booking.getStatus().name());
        return dto;
    }
}
