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

    @PostMapping
    public Mono<String> createRole(@RequestBody String roleJson) {
        return roleService.createRole(roleJson);
    }

    @GetMapping("/{idUser}")
    public Mono<String> getRoles(@PathVariable String idUser) {
        return roleService.getRolesByUserId(idUser);
    }

    @PutMapping
    public Mono<String> updateRole(@RequestBody String roleJson) {
        return roleService.updateRole(roleJson);
    }

    @DeleteMapping
    public Mono<String> deleteRole(@RequestParam String idUser, @RequestParam String idRol) {
        return roleService.deleteRole(idUser, idRol);
    }
}
