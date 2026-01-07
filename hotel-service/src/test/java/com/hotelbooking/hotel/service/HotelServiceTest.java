package com.hotelbooking.hotel.service;

import com.hotelbooking.hotel.dto.HotelDto;
import com.hotelbooking.hotel.entity.Hotel;
import com.hotelbooking.hotel.mapper.HotelMapper;
import com.hotelbooking.hotel.repository.HotelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Тесты для HotelService
 */
@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelMapper hotelMapper;

    @InjectMocks
    private HotelService hotelService;

    @Test
    void getAllHotels_ShouldReturnHotelList() {
        // Given
        Hotel hotel = new Hotel(1L, "Test Hotel", "Test Address", null);
        HotelDto hotelDto = new HotelDto(1L, "Test Hotel", "Test Address");

        when(hotelRepository.findAll()).thenReturn(List.of(hotel));
        when(hotelMapper.toDto(hotel)).thenReturn(hotelDto);

        // When
        List<HotelDto> result = hotelService.getAllHotels();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Hotel");
    }

    @Test
    void getHotelById_ShouldReturnHotel_WhenExists() {
        // Given
        Hotel hotel = new Hotel(1L, "Test Hotel", "Test Address", null);
        HotelDto hotelDto = new HotelDto(1L, "Test Hotel", "Test Address");

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelMapper.toDto(hotel)).thenReturn(hotelDto);

        // When
        HotelDto result = hotelService.getHotelById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Hotel");
    }

    @Test
    void getHotelById_ShouldThrowException_WhenNotExists() {
        // Given
        when(hotelRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> hotelService.getHotelById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Отель не найден: 1");
    }

    @Test
    void createHotel_ShouldReturnCreatedHotel() {
        // Given
        HotelDto inputDto = new HotelDto(null, "New Hotel", "New Address");
        Hotel savedHotel = new Hotel(1L, "New Hotel", "New Address", null);
        HotelDto outputDto = new HotelDto(1L, "New Hotel", "New Address");

        when(hotelMapper.toEntity(inputDto)).thenReturn(new Hotel(null, "New Hotel", "New Address", null));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);
        when(hotelMapper.toDto(savedHotel)).thenReturn(outputDto);

        // When
        HotelDto result = hotelService.createHotel(inputDto);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("New Hotel");
    }
}
