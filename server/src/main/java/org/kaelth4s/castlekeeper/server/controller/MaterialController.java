package org.kaelth4s.castlekeeper.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.kaelth4s.castlekeeper.server.model.Material;
import org.kaelth4s.castlekeeper.server.service.MaterialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MaterialController {
    private final MaterialService service;

    public MaterialController(MaterialService service) {
        this.service = service;
    }

    @GetMapping("/materials")
    public List<Material> getAll() {
        return service.getAll();
    }

    @GetMapping("/materials/{id}")
    public ResponseEntity<Material> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponse(responseCode = "201", description = "OK")
    @PostMapping("/materials")
    public ResponseEntity<Material> create(@Valid @RequestBody Material material) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(material));
    }

    @PutMapping("/materials/{id}")
    public ResponseEntity<Material> update(@PathVariable Long id, @Valid @RequestBody Material material) {
        return ResponseEntity.ok(service.update(id, material));
    }

    @ApiResponse(responseCode = "204", description = "OK")
    @DeleteMapping("/materials/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
