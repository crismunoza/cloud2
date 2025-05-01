package com.example.bff;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import com.example.bff.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    public Mono<String> getAllUsers() {
        return userService.getAllUsers();
    }
    
    @GetMapping("/{id}")
    public Mono<String> getUser(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @PostMapping("/event")
    public Mono<String> sendUserEvent(@RequestBody String userJson) {
        return userService.sendUserEvent(userJson);
    }
}
