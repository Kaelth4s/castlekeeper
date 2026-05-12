package org.kaelth4s.castlekeeper.server.service;

import org.kaelth4s.castlekeeper.server.exception.ResourceNotFoundException;
import org.kaelth4s.castlekeeper.server.model.Material;
import org.kaelth4s.castlekeeper.server.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MaterialService {
    private final MaterialRepository repository;

    public MaterialService(MaterialRepository repository) {
        this.repository = repository;
    }

    public List<Material> getAll() {
        return repository.findAll();
    }

    public Optional<Material> getById(Long id) {
        return repository.findById(id);
    }

    public Material create(Material material) {
        material.setId(null);
        return repository.save(material);
    }

    public Material update(Long id, Material updated) {
        return repository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            return repository.save(existing);
        }).orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + id));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Material not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
