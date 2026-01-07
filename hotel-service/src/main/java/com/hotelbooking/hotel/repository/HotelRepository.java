package com.hotelbooking.hotel.repository;

import com.hotelbooking.hotel.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с отелями
 */
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
