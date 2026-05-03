package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId, @Valid @RequestBody ItemDto itemDto) {
        return itemDto;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemDto itemDto, @PathVariable Long itemId) {
        itemDto.setId(itemId);
        return itemDto;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable Long itemId) {
        return new ItemDto();
    }

    @GetMapping
    public List<ItemDto> getAllItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return Collections.emptyList();
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return Collections.emptyList();
    }
}