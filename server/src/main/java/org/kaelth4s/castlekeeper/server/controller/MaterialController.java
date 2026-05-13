package org.kaelth4s.castlekeeper.server.controller;

import jakarta.validation.Valid;
import org.kaelth4s.castlekeeper.server.dto.MaterialRequest;
import org.kaelth4s.castlekeeper.server.dto.MaterialResponse;
import org.kaelth4s.castlekeeper.server.service.MaterialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MaterialController {
    private final MaterialService service;

    public MaterialController(MaterialService service) {
        this.service = service;
    }

    @GetMapping("/materials")
    public List<MaterialResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/materials/{id}")
    public ResponseEntity<MaterialResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/materials")
    public ResponseEntity<MaterialResponse> create(@Valid @RequestBody MaterialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/materials/{id}")
    public ResponseEntity<MaterialResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody MaterialRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/materials/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
