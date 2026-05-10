package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        User user = getUserById(userId);
        ItemRequest request = ItemRequestMapper.toItemRequest(dto, user);
        ItemRequest savedRequest = itemRequestRepository.save(request);
        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    public List<ItemRequestDto> getUserRequests(Long userId) {
        getUserById(userId);
        return itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        getUserById(userId);
        int page = from / size;
        return itemRequestRepository.findAllByRequesterIdNotOrderByCreatedDesc(
                        userId,
                        PageRequest.of(page, size))
                .stream()
                .map(this::toDtoWithItems)
                .collect(Collectors.toList());
    }

    public ItemRequestDto getById(Long userId, Long requestId) {
        getUserById(userId);
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        return toDtoWithItems(request);
    }

    private ItemRequestDto toDtoWithItems(ItemRequest request) {
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        List<Item> items = itemRepository.findAllByRequestId(request.getId());

        dto.setItems(items.stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList()));

        return dto;
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}
