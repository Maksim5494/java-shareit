package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(new ArrayList<>()) // Список заполняется отдельно в сервисе
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto dto, User requester) {
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());
        return request;
    }
}
