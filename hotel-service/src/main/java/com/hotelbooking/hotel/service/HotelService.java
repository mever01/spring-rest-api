package com.hotelbooking.hotel.service;

import com.hotelbooking.hotel.dto.HotelDto;
import com.hotelbooking.hotel.entity.Hotel;
import com.hotelbooking.hotel.mapper.HotelMapper;
import com.hotelbooking.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления отелями
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    /**
     * Получить все отели
     */
    public List<HotelDto> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить отель по ID
     */
    public HotelDto getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отель не найден: " + id));
        return hotelMapper.toDto(hotel);
    }

    /**
     * Создать отель
     */
    @Transactional
    public HotelDto createHotel(HotelDto hotelDto) {
        Hotel hotel = hotelMapper.toEntity(hotelDto);
        hotel = hotelRepository.save(hotel);
        return hotelMapper.toDto(hotel);
    }

    /**
     * Обновить отель
     */
    @Transactional
    public HotelDto updateHotel(Long id, HotelDto hotelDto) {
        Hotel existingHotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отель не найден: " + id));

        existingHotel.setName(hotelDto.getName());
        existingHotel.setAddress(hotelDto.getAddress());

        existingHotel = hotelRepository.save(existingHotel);
        return hotelMapper.toDto(existingHotel);
    }

    /**
     * Удалить отель
     */
    @Transactional
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new RuntimeException("Отель не найден: " + id);
        }
        hotelRepository.deleteById(id);
    }
}


