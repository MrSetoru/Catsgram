package ru.yandex.practicum.catsgram.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.service.PostService;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public Collection<Post> findAll(@RequestParam(value = "sort", defaultValue = "Значение " +
            "по умолчанию") String sort,
                                    @RequestParam(value = "from", defaultValue = "0") int from,
                                    @RequestParam(value = "size", defaultValue = "10") int size) {
        PostService.SortOrder sortOrder = PostService.SortOrder.from(sort);
        return postService.findAll(sortOrder, from, size);
    }

    @GetMapping("/{id}")
    public Optional<Post> findPostById(@PathVariable Long id) {
        Optional<Post> post = postService.findPostById(id);
        if (post.isPresent()) {
            return post;
        } else {
            throw new NotFoundException("Пост с идентификатором " + id + " не найден");
        }
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post create(@RequestBody Post post) {
        return postService.create(post);
    }

    @PutMapping
    public Post update(@RequestBody Post newPost) {
        return postService.update(newPost);
    }
}