package org.kaelth4s.castlekeeper.server.controller;

import jakarta.validation.Valid;
import org.kaelth4s.castlekeeper.server.dto.AuthorTypeRequest;
import org.kaelth4s.castlekeeper.server.dto.AuthorTypeResponse;
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
    public List<AuthorTypeResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/author-types/{id}")
    public ResponseEntity<AuthorTypeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/author-types")
    public ResponseEntity<AuthorTypeResponse> create(@Valid @RequestBody AuthorTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/author-types/{id}")
    public ResponseEntity<AuthorTypeResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody AuthorTypeRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/author-types/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
