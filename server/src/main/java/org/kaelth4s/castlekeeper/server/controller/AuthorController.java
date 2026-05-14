package org.kaelth4s.castlekeeper.server.controller;

import jakarta.validation.Valid;
import org.kaelth4s.castlekeeper.dto.AuthorRequest;
import org.kaelth4s.castlekeeper.dto.AuthorResponse;
import org.kaelth4s.castlekeeper.server.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AuthorController {
    private final AuthorService service;

    public AuthorController(AuthorService service) {
        this.service = service;
    }

    @GetMapping("/authors")
    public List<AuthorResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/authors/{id}")
    public ResponseEntity<AuthorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/authors")
    public ResponseEntity<AuthorResponse> create(@Valid @RequestBody AuthorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/authors/{id}")
    public ResponseEntity<AuthorResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody AuthorRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/authors/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
