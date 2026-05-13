package org.kaelth4s.castlekeeper.server.service;

import org.kaelth4s.castlekeeper.server.dto.MaterialRequest;
import org.kaelth4s.castlekeeper.server.dto.MaterialResponse;
import org.kaelth4s.castlekeeper.server.exception.ResourceNotFoundException;
import org.kaelth4s.castlekeeper.server.model.Material;
import org.kaelth4s.castlekeeper.server.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MaterialService {
    private final MaterialRepository repository;

    public MaterialService(MaterialRepository repository) {
        this.repository = repository;
    }

    public List<MaterialResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public MaterialResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + id));
    }

    public MaterialResponse create(MaterialRequest request) {
        Material entity = new Material();
        entity.setName(request.getName());
        return toResponse(repository.save(entity));
    }

    public MaterialResponse update(Long id, MaterialRequest request) {
        return repository.findById(id).map(existing -> {
            existing.setName(request.getName());
            return toResponse(repository.save(existing));
        }).orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + id));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Material not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private MaterialResponse toResponse(Material entity) {
        return new MaterialResponse(entity.getId(), entity.getName());
    }
}
