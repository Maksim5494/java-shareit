package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemMapper.toEntity(itemDto);
        item.setOwner(owner);

        item = itemRepository.save(item);
        ItemDto dto = itemMapper.toDto(item);
        dto.setComments(Collections.emptyList());
        return dto;
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        item = itemRepository.save(item);
        ItemDto dto = itemMapper.toDto(item);
        setBookings(dto, item);
        setComments(dto, item);

        return dto;
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemDto dto = itemMapper.toDto(item);

        if (userId != null && item.getOwner() != null && item.getOwner().getId().equals(userId)) {
            setBookings(dto, item);
        }

        setComments(dto, item);
        return dto;
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return itemRepository.findAllByOwner_IdOrderByIdAsc(userId).stream()
                .map(item -> {
                    ItemDto dto = itemMapper.toDto(item);
                    setBookings(dto, item);
                    setComments(dto, item);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.searchAvailableByText(text).stream()
                .map(item -> {
                    ItemDto dto = itemMapper.toDto(item);
                    dto.setComments(Collections.emptyList());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void setBookings(ItemDto dto, Item item) {
        LocalDateTime now = LocalDateTime.now();

        Booking lastBooking = bookingRepository
                .findFirstByItem_IdAndStartBeforeAndStatusOrderByStartDesc(
                        item.getId(), now, BookingStatus.APPROVED
                )
                .orElse(null);

        Booking nextBooking = bookingRepository
                .findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(
                        item.getId(), now, BookingStatus.APPROVED
                )
                .orElse(null);

        dto.setLastBooking(lastBooking != null
                ? BookingShortDto.builder()
                .id(lastBooking.getId())
                .bookerId(lastBooking.getBooker().getId())
                .build()
                : null);

        dto.setNextBooking(nextBooking != null
                ? BookingShortDto.builder()
                .id(nextBooking.getId())
                .bookerId(nextBooking.getBooker().getId())
                .build()
                : null);
    }

    private void setComments(ItemDto dto, Item item) {
        List<CommentDto> comments = commentRepository.findAllByItem_IdOrderByCreatedDesc(item.getId())
                .stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());

        dto.setComments(comments);
    }
}