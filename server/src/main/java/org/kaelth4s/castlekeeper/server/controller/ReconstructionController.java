package org.kaelth4s.castlekeeper.server.controller;

import jakarta.validation.Valid;
import org.kaelth4s.castlekeeper.server.dto.ReconstructionRequest;
import org.kaelth4s.castlekeeper.server.dto.ReconstructionResponse;
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
    public List<ReconstructionResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/reconstructions/{id}")
    public ResponseEntity<ReconstructionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/reconstructions")
    public ResponseEntity<ReconstructionResponse> create(@Valid @RequestBody ReconstructionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/reconstructions/{id}")
    public ResponseEntity<ReconstructionResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody ReconstructionRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/reconstructions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
