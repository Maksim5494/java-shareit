package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;

@UtilityClass
public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(new BookingDto.Item(booking.getItem().getId(), booking.getItem().getName()))
                .booker(new BookingDto.Booker(booking.getBooker().getId()))
                .build();
    }

    public static Booking toBooking(BookingInputDto bookingInputDto) {
        return Booking.builder()
                .start(bookingInputDto.getStart())
                .end(bookingInputDto.getEnd())
                .build();
    }
}
