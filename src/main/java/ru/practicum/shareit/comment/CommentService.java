package ru.practicum.shareit.comment;

public interface CommentService {
    CommentDto create(Long userId, Long itemId, String text);
}
