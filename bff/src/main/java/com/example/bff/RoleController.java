package com.example.bff;

import org.springframework.web.bind.annotation.*;
import com.example.bff.services.RoleService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/all")
    public Mono<String> getAllRol() {
        return roleService.getAllRol();
    }

    @GetMapping("/{idUser}")
    public Mono<String> getRoles(@PathVariable String idUser) {
        return roleService.getRolesByUserId(idUser);
    }

    @PostMapping("/event")
    public Mono<String> sendUserEvent(@RequestBody String userJson) {
        return roleService.sendUserEvent(userJson);
    }
}
