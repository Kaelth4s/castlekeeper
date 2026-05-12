package org.kaelth4s.castlekeeper.server.controller;

import org.kaelth4s.castlekeeper.server.model.AuthorType;
import org.kaelth4s.castlekeeper.server.service.AuthorTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AuthorTypeController {
    private final AuthorTypeService service;

    public AuthorTypeController(AuthorTypeService service) {
        this.service = service;
    }

    @GetMapping("/author-types")
    public List<AuthorType> getAll() {
        return service.getAll();
    }

    @GetMapping("/author-types/{id}")
    public ResponseEntity<AuthorType> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/author-types")
    public ResponseEntity<AuthorType> create(@RequestBody AuthorType authorType) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(authorType));
    }

    @PutMapping("/author-types/{id}")
    public ResponseEntity<AuthorType> update(@PathVariable Long id, @RequestBody AuthorType authorType) {
        return ResponseEntity.ok(service.update(id, authorType));
    }

    @DeleteMapping("/author-types/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
