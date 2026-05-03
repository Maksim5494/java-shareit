package ru.practicum.shareit.user.model;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/users")
public class UserController {

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        return userDto;
    }

    @PatchMapping("/{userId}")
    public UserDto update(@RequestBody UserDto userDto, @PathVariable Long userId) {
        userDto.setId(userId);
        return userDto;
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable Long userId) {
        return new UserDto();
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
    }
}
