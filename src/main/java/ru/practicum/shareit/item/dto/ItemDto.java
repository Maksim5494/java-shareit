package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.comment.CommentDto;

import java.util.List;

@Data
@Builder
public class ItemDto {

    private Long id;

    @NotBlank(groups = Create.class)
    private String name;

    @NotBlank(groups = Create.class)
    private String description;

    @NotNull(groups = Create.class)
    private Boolean available;

    private Long requestId;
    private Long ownerId;

    private Long lastBookingId;
    private Long nextBookingId;
    private List<CommentDto> comments;

    public interface Create {}
}