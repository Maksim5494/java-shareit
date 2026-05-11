package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
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
import java.util.List;
import java.util.Objects;
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

        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException("Вещь не найдена");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemDto dto = itemMapper.toDto(item);

        List<CommentDto> comments = commentRepository.findAllByItem_IdOrderByCreatedAsc(itemId).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
        dto.setComments(comments);

        if (item.getOwner() != null && Objects.equals(item.getOwner().getId(), userId)) {
            LocalDateTime now = LocalDateTime.now();

            bookingRepository.findFirstByItem_IdAndStartBeforeAndStatusOrderByStartDesc(itemId, now, BookingStatus.APPROVED)
                    .ifPresent(lastBooking -> dto.setLastBooking(BookingShortDto.builder()
                            .id(lastBooking.getId())
                            .bookerId(lastBooking.getBooker().getId())
                            .build()));

            bookingRepository.findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(itemId, now, BookingStatus.APPROVED)
                    .ifPresent(nextBooking -> dto.setNextBooking(BookingShortDto.builder()
                            .id(nextBooking.getId())
                            .bookerId(nextBooking.getBooker().getId())
                            .build()));
        }

        return dto;
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return itemRepository.findAllByOwner_IdOrderByIdAsc(userId).stream()
                .map(itemMapper::toDto)
                .peek(dto -> dto.setComments(commentRepository.findAllByItem_IdOrderByCreatedAsc(dto.getId()).stream()
                        .map(commentMapper::toDto)
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }
}