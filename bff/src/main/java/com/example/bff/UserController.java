package com.example.bff;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.bff.services.UserService;


@RestController
@RequestMapping("/users")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public Mono<String> getUser(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public Mono<String> createUser(@RequestBody String userJson) {
        return userService.createUser(userJson);
    }

    @PutMapping("/{id}")
    public Mono<String> updateUser(@PathVariable String id, @RequestBody String userJson) {
        return userService.updateUser(id, userJson);
    }

    @DeleteMapping("/{id}")
    public Mono<String> deleteUser(@PathVariable String id) {
        return userService.deleteUser(id);
    }
}
