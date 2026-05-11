package ru.practicum.shareit.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto create(Long userId, Long itemId, String text) {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        LocalDateTime now = LocalDateTime.now();

        boolean canComment = bookingRepository
                .findFirstByItem_IdAndStartBeforeAndStatusOrderByStartDesc(itemId, now, BookingStatus.APPROVED)
                .filter(booking -> booking.getBooker().getId().equals(userId))
                .isPresent();

        if (!canComment) {
            throw new ValidationException("Пользователь не может оставить комментарий без завершенного бронирования");
        }

        Comment comment = Comment.builder()
                .text(text)
                .item(item)
                .author(author)
                .created(now)
                .build();

        return commentMapper.toDto(commentRepository.save(comment));
    }
}