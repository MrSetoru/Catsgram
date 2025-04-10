package ru.yandex.practicum.catsgram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PostService {
    private final Map<Long, Post> posts = new HashMap<>();
    private final UserService userService;

    @Autowired
    public PostService(UserService userService) {
        this.userService = userService;
    }

    public Collection<Post> findAll(SortOrder sortOrder, int from, int size) {
        Stream<Post> postStream = posts.values().stream();

        if (sortOrder == SortOrder.ASCENDING) {
            postStream = postStream.sorted(Comparator.comparing(Post::getPostDate));
        } else {
            postStream = postStream.sorted(Comparator.comparing(Post::getPostDate).reversed());
        }
        if (from < 0 || size <= 0) {
            return Collections.emptyList();
        }
        return postStream
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    public Post create(Post post) {
        if (userService.findUserById(post.getAuthorId()).isPresent()) {
            post.setAuthorId(post.getAuthorId());
        } else {
            throw new NotFoundException("Автор с id = " + post.getAuthorId() + " не найден");
        }

        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }
        post.setId(getNextId());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");
    }

    public Optional<Post> findPostById(Long id) {
        if (posts.containsKey(id)) {
            return Optional.of(posts.get(id));
        } else {
            return Optional.empty();
        }
    }

    public enum SortOrder {
        ASCENDING, DESCENDING;

        public static SortOrder from(String order) {
            switch (order.toLowerCase()) {
                case "ascending":
                case "asc":
                    return ASCENDING;
                case "descending":
                case "desc":
                    return DESCENDING;
                default:
                    return DESCENDING; // Default to descending if invalid input
            }
        }
    }

    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}