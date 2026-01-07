package com.hotelbooking.hotel.service;

import com.hotelbooking.hotel.dto.CreateRoomRequest;
import com.hotelbooking.hotel.dto.RoomDto;
import com.hotelbooking.hotel.entity.Hotel;
import com.hotelbooking.hotel.entity.Room;
import com.hotelbooking.hotel.mapper.RoomMapper;
import com.hotelbooking.hotel.repository.HotelRepository;
import com.hotelbooking.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления номерами с алгоритмом планирования занятости
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;

    /**
     * Получить все доступные номера (без сортировки)
     */
    public List<RoomDto> getAllAvailableRooms() {
        return roomRepository.findByAvailableTrue().stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить рекомендованные номера (сортировка по times_booked для равномерной загрузки)
     * Алгоритм планирования: сортировка по возрастанию times_booked, затем по id
     */
    public List<RoomDto> getRecommendedRooms() {
        return roomRepository.findAvailableRoomsSortedByTimesBooked().stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить все номера по отелю
     */
    public List<RoomDto> getRoomsByHotel(Long hotelId) {
        return roomRepository.findByHotelId(hotelId).stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Создать номер
     */
    @Transactional
    public RoomDto createRoom(CreateRoomRequest request) {
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new RuntimeException("Отель не найден: " + request.getHotelId()));

        // Проверить, что номер с таким номером не существует в этом отеле
        boolean roomExists = hotel.getRooms().stream()
                .anyMatch(room -> room.getNumber().equals(request.getNumber()));

        if (roomExists) {
            throw new RuntimeException("Номер " + request.getNumber() + " уже существует в отеле " + hotel.getName());
        }

        Room room = roomMapper.toEntity(new RoomDto(null, request.getNumber(), request.getHotelId(), true, 0));
        room.setHotel(hotel); // Устанавливаем hotel вручную

        room = roomRepository.save(room);
        return roomMapper.toDto(room);
    }

    /**
     * Подтвердить доступность номера (временная блокировка для саги)
     */
    @Transactional
    public boolean confirmRoomAvailability(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Номер не найден: " + roomId));

        if (!room.getAvailable()) {
            return false; // Номер недоступен
        }

        // В реальности здесь можно добавить временную блокировку
        // Для простоты просто проверяем доступность
        return true;
    }

    /**
     * Снять блокировку номера (компенсирующее действие)
     */
    @Transactional
    public void releaseRoomBlock(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Номер не найден: " + roomId));

        // В реальности здесь снимаем временную блокировку
        // Для простоты ничего не делаем, так как блокировка не реализована
    }

    /**
     * Увеличить счетчик бронирований номера (после успешного бронирования)
     */
    @Transactional
    public void incrementTimesBooked(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Номер не найден: " + roomId));

        room.setTimesBooked(room.getTimesBooked() + 1);
        roomRepository.save(room);
    }

    /**
     * Получить статистику загрузки номеров
     */
    public List<RoomDto> getRoomStatistics() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }
}
