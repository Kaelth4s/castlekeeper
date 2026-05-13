package org.kaelth4s.castlekeeper.server.controller;

import jakarta.validation.Valid;
import org.kaelth4s.castlekeeper.server.dto.CastleRequest;
import org.kaelth4s.castlekeeper.server.dto.CastleResponse;
import org.kaelth4s.castlekeeper.server.service.CastleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CastleController {
    private final CastleService service;

    public CastleController(CastleService service) {
        this.service = service;
    }

    @GetMapping("/castles")
    public List<CastleResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/castles/{id}")
    public ResponseEntity<CastleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/castles/random")
    public ResponseEntity<CastleResponse> getRandom() {
        return service.getRandom()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/castles")
    public ResponseEntity<CastleResponse> create(@Valid @RequestBody CastleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/castles/{id}")
    public ResponseEntity<CastleResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody CastleRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/castles/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
