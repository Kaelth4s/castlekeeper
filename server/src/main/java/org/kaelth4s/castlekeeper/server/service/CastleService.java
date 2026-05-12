package org.kaelth4s.castlekeeper.server.service;

import org.kaelth4s.castlekeeper.server.model.Castle;
import org.kaelth4s.castlekeeper.server.repository.CastleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CastleService {
    private final CastleRepository repository;

    public CastleService(CastleRepository repository) {
        this.repository = repository;
    }

    public List<Castle> getAll() {
        return repository.findAll();
    }

    public Optional<Castle> getById(Long id) {
        return repository.findById(id);
    }

    public Castle create(Castle castle) {
        return repository.save(castle);
    }

    public Castle update(Long id, Castle updated) {
        return repository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            existing.setAuthor(updated.getAuthor());
            existing.setBuiltYear(updated.getBuiltYear());
            existing.setDestroyedYear(updated.getDestroyedYear());
            existing.setHeightM(updated.getHeightM());
            existing.setMaterial(updated.getMaterial());
            return repository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Castle not found: " + id));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Optional<Castle> getRandom() {
        return repository.findRandom();
    }
}

