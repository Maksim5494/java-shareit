package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public BookingDto create(Long userId, BookingDto bookingDto) {
        if (bookingDto.getItemId() == null
                || bookingDto.getStart() == null
                || bookingDto.getEnd() == null
                || !bookingDto.getStart().isAfter(LocalDateTime.now())
                || !bookingDto.getEnd().isAfter(bookingDto.getStart())) {
            throw new ValidationException("Некорректные даты бронирования");
        }

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new ValidationException("Нельзя бронировать свою вещь");
        }

        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(BookingStatus.WAITING);

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto approve(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Только владелец вещи может подтверждать бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования уже изменен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Бронирование не найдено");
        }

        return BookingMapper.toDto(booking);
    }

    public List<BookingDto> getByBooker(Long userId, String state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findAllByBooker_IdOrderByStartDesc(userId);
            case "CURRENT" -> bookingRepository.findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case "PAST" -> bookingRepository.findAllByBooker_IdAndEndBeforeOrderByStartDesc(userId, now);
            case "FUTURE" -> bookingRepository.findAllByBooker_IdAndStartAfterOrderByStartDesc(userId, now);
            case "WAITING" -> bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case "REJECTED" -> bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> throw new ValidationException("Unknown state: " + state);
        };

        return bookings.stream().map(BookingMapper::toDto).toList();
    }

    public List<BookingDto> getByOwner(Long userId, String state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
            case "CURRENT" -> bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case "PAST" -> bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
            case "FUTURE" -> bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
            case "WAITING" -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case "REJECTED" -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> throw new ValidationException("Unknown state: " + state);
        };

        return bookings.stream().map(BookingMapper::toDto).toList();
    }
}