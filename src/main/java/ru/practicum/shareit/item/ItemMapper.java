package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

@Component
public class ItemMapper {

    public Item toItem(ItemDto dto) {
        if (dto == null) {
            return null;
        }
        return new Item(dto.getId(), dto.getName(), dto.getDescription(), dto.getAvailable(), null, dto.getRequestId());
    }

    public ItemDto toDto(Item item) {
        if (item == null) {
            return null;
        }
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable(), item.getRequestId());
    }
}
