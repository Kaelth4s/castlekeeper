package org.kaelth4s.castlekeeper.server.controller;

import jakarta.validation.Valid;
import org.kaelth4s.castlekeeper.server.model.Author;
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
    public List<Author> getAll() {
        return service.getAll();
    }

    @GetMapping("/authors/{id}")
    public ResponseEntity<Author> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/authors")
    public ResponseEntity<Author> create(@Valid @RequestBody Author author) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(author));
    }

    @PutMapping("/authors/{id}")
    public ResponseEntity<Author> update(@Valid @PathVariable Long id, @RequestBody Author author) {
        return ResponseEntity.ok(service.update(id, author));
    }

    @DeleteMapping("/authors/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
