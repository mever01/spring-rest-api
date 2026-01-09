package com.hotelbooking.hotel.config;

import com.hotelbooking.hotel.entity.Hotel;
import com.hotelbooking.hotel.entity.Room;
import com.hotelbooking.hotel.repository.HotelRepository;
import com.hotelbooking.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Инициализация тестовых данных после запуска приложения
 */
@Component
@RequiredArgsConstructor
@Profile("!test") // Не выполнять в тестах
public class DataInitializer implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @Override
    public void run(String... args) throws Exception {
        if (hotelRepository.count() == 0) {
            initializeData();
        }
    }

    private void initializeData() {
        // Создание отелей
        Hotel hotel1 = new Hotel();
        hotel1.setName("Гранд Отель Москва");
        hotel1.setAddress("ул. Тверская, 10, Москва");
        hotel1 = hotelRepository.save(hotel1);

        Hotel hotel2 = new Hotel();
        hotel2.setName("Спорт Отель");
        hotel2.setAddress("пр. Ленина, 25, Санкт-Петербург");
        hotel2 = hotelRepository.save(hotel2);

        Hotel hotel3 = new Hotel();
        hotel3.setName("Бизнес Центр");
        hotel3.setAddress("ул. Пушкина, 15, Екатеринбург");
        hotel3 = hotelRepository.save(hotel3);

        // Создание номеров для первого отеля
        createRoom(hotel1, "101");
        createRoom(hotel1, "102");
        createRoom(hotel1, "103");
        createRoom(hotel1, "201");
        createRoom(hotel1, "202");

        // Создание номеров для второго отеля
        createRoom(hotel2, "101");
        createRoom(hotel2, "102");
        createRoom(hotel2, "201");

        // Создание номеров для третьего отеля
        createRoom(hotel3, "101");
        createRoom(hotel3, "102");
    }

    private void createRoom(Hotel hotel, String number) {
        Room room = new Room();
        room.setNumber(number);
        room.setHotel(hotel);
        room.setAvailable(true);
        room.setTimesBooked(0);
        roomRepository.save(room);
    }
}


