package it.alnao.esempio04.controller;

import it.alnao.esempio04.service.UserDynamoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/apid/users")
public class UserControllerDynamo {

    private final UserDynamoService userService;

    public UserControllerDynamo(UserDynamoService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        Object user = userService.createUser(request.name(), request.email());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<?>> getAllUsers() {
        List<?> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is healthy");
    }

    public record CreateUserRequest(
            @NotBlank(message = "Name is required") String name,
            @Email(message = "Email should be valid") @NotBlank(message = "Email is required") String email
    ) {}
}