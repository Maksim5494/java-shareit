package ru.practicum.shareit.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));

        boolean hasBooked = bookingRepository.existsByBooker_IdAndItem_IdAndStatusAndEndBefore(
                userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());

        if (!hasBooked) {
            throw new ValidationException("Вы не можете оставить отзыв на вещь, которую арендовали");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        comment = commentRepository.save(comment);

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(user.getName())
                .created(comment.getCreated())
                .build();
    }
}