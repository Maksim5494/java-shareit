package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingShortDto;
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

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BookingShortDto lastBooking;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BookingShortDto nextBooking;

    private List<CommentDto> comments;

    public interface Create {}
}