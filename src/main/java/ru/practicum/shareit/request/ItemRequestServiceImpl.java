package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        User user = getUserById(userId);
        ItemRequest request = ItemRequestMapper.toItemRequest(dto, user);
        ItemRequest savedRequest = itemRequestRepository.save(request);
        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        getUserById(userId);

        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);
        Map<Long, List<ItemDto>> itemsByRequestId = getItemsByRequestIds(requests);

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        getUserById(userId);

        PageRequest pageRequest = PageRequest.of(from / size, size);

        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequesterIdNotOrderByCreatedDesc(userId, pageRequest);

        Map<Long, List<ItemDto>> itemsByRequestId = getItemsByRequestIds(requests);

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        getUserById(userId);

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        List<ItemDto> items = itemRepository.findAllByRequestId(requestId).stream()
                .map(itemMapper::toDto)
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());

        return ItemRequestMapper.toItemRequestDto(request, items);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private Map<Long, List<ItemDto>> getItemsByRequestIds(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        if (requestIds.isEmpty()) {
            return Map.of();
        }

        return itemRepository.findAllByRequestIdIn(requestIds).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.groupingBy(ItemDto::getRequestId));
    }
}
