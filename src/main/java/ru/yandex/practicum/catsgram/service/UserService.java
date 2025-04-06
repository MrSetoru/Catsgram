package ru.yandex.practicum.catsgram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.controller.UserController;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        return users.values();
    }

    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        } else if (isEmailAlreadyRegistered(user.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        user.setId(getNextId());
        user.setUsername(user.getUsername());
        user.setEmail(user.getEmail());
        user.setPassword(user.getPassword());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        return user;
    }

    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (isEmailAlreadyRegistered(newUser.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
            if (newUser.getEmail() == null) {
                oldUser.setEmail(oldUser.getEmail());
            } else {
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getUsername() == null) {
                oldUser.setUsername(oldUser.getUsername());
            } else {
                oldUser.setUsername(newUser.getUsername());
            }
            if (newUser.getPassword() == null) {
                oldUser.setPassword(oldUser.getPassword());
            } else {
                oldUser.setPassword(newUser.getPassword());
            }
            oldUser.setRegistrationDate(Instant.now());
            users.put(oldUser.getId(), oldUser);
            return oldUser;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    public Optional<User> findUserById(Long id) {
        if (users.containsKey(id)) {
            return Optional.of(users.get(id));
        } else {
            return Optional.empty();
        }
    }

    private boolean isEmailAlreadyRegistered(String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equals(email));
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }


}
