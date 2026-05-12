package org.kaelth4s.castlekeeper.server.controller;

import org.kaelth4s.castlekeeper.server.model.Reconstruction;
import org.kaelth4s.castlekeeper.server.service.ReconstructionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReconstructionController {
    private final ReconstructionService service;

    public ReconstructionController(ReconstructionService service) {
        this.service = service;
    }

    @GetMapping("/reconstructions")
    public List<Reconstruction> getAll() {
        return service.getAll();
    }

    @GetMapping("/reconstructions/{id}")
    public ResponseEntity<Reconstruction> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/reconstructions")
    public ResponseEntity<Reconstruction> create(@RequestBody Reconstruction reconstruction) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(reconstruction));
    }

    @PutMapping("/reconstructions/{id}")
    public ResponseEntity<Reconstruction> update(@PathVariable Long id, @RequestBody Reconstruction reconstruction) {
        return ResponseEntity.ok(service.update(id, reconstruction));
    }

    @DeleteMapping("/reconstructions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
