package com.hotelbooking.hotel.repository;

import com.hotelbooking.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Репозиторий для работы с номерами
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Найти все доступные номера (available = true)
     */
    List<Room> findByAvailableTrue();

    /**
     * Найти все доступные номера, отсортированные по times_booked (возрастание), затем по id
     */
    @Query("SELECT r FROM Room r WHERE r.available = true ORDER BY r.timesBooked ASC, r.id ASC")
    List<Room> findAvailableRoomsSortedByTimesBooked();

    /**
     * Найти номера по отелю
     */
    List<Room> findByHotelId(Long hotelId);

    /**
     * Проверить доступность номера на указанные даты
     * (упрощенная проверка - в реальности нужна таблица бронирований)
     */
    @Query("SELECT COUNT(r) = 0 FROM Room r WHERE r.id = :roomId AND r.available = true")
    boolean isRoomAvailableForBooking(@Param("roomId") Long roomId);
}
