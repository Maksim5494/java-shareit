package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long idCounter = 1L;

    @Override
    public Item save(Item item) {
        item.setId(idCounter++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAllByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .sorted(Comparator.comparing(Item::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchAvailableByText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String lowerText = text.toLowerCase();

        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> {
                    String name = item.getName() != null ? item.getName().toLowerCase() : "";
                    String description = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
                    return name.contains(lowerText) || description.contains(lowerText);
                })
                .collect(Collectors.toList());
    }
}