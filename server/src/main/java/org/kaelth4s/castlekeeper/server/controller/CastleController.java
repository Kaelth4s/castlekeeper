package org.kaelth4s.castlekeeper.server.controller;

import org.kaelth4s.castlekeeper.server.model.Castle;
import org.kaelth4s.castlekeeper.server.service.CastleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CastleController {
    private final CastleService service;

    public CastleController(CastleService service) {
        this.service = service;
    }

    @GetMapping("/castles")
    public List<Castle> getAll() {
        return service.getAll();
    }

    @GetMapping("/castles/{id}")
    public ResponseEntity<Castle> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/castles/random")
    public Optional<Castle> getRandom() {
        return service.getRandom();
    }

    @PostMapping("/castles")
    public ResponseEntity<Castle> create(@RequestBody Castle castle) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(castle));
    }

    @PutMapping("/castles/{id}")
    public ResponseEntity<Castle> update(@PathVariable Long id, @RequestBody Castle castle) {
        return ResponseEntity.ok(service.update(id, castle));
    }

    @DeleteMapping("/castles/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
