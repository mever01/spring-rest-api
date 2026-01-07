package com.hotelbooking.booking.repository;

import com.hotelbooking.booking.entity.Booking;
import com.hotelbooking.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с бронированиями
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserOrderByCreatedAtDesc(User user);

    List<Booking> findByUserAndStatus(User user, Booking.Status status);

    // Проверка пересечения дат для номера (упрощенная версия)
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.roomId = :roomId AND b.status = 'CONFIRMED' AND " +
           "((b.startDate <= :endDate AND b.endDate >= :startDate))")
    boolean existsOverlappingBooking(@Param("roomId") Long roomId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    // Для идемпотентности - проверка существующего запроса
    Optional<Booking> findByRequestId(String requestId);
}
