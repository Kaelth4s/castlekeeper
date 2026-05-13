package org.kaelth4s.castlekeeper.server.service;

import org.kaelth4s.castlekeeper.server.dto.AuthorTypeRequest;
import org.kaelth4s.castlekeeper.server.dto.AuthorTypeResponse;
import org.kaelth4s.castlekeeper.server.exception.ResourceNotFoundException;
import org.kaelth4s.castlekeeper.server.model.AuthorType;
import org.kaelth4s.castlekeeper.server.repository.AuthorTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuthorTypeService {
    private final AuthorTypeRepository repository;

    public AuthorTypeService(AuthorTypeRepository repository) {
        this.repository = repository;
    }

    public List<AuthorTypeResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public AuthorTypeResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("AuthorType not found with id: " + id));
    }

    public AuthorTypeResponse create(AuthorTypeRequest request) {
        AuthorType entity = new AuthorType();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        return toResponse(repository.save(entity));
    }

    public AuthorTypeResponse update(Long id, AuthorTypeRequest request) {
        return repository.findById(id).map(existing -> {
            existing.setName(request.getName());
            existing.setDescription(request.getDescription());
            return toResponse(repository.save(existing));
        }).orElseThrow(() -> new ResourceNotFoundException("AuthorType not found with id: " + id));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("AuthorType not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private AuthorTypeResponse toResponse(AuthorType entity) {
        return new AuthorTypeResponse(entity.getId(), entity.getName(), entity.getDescription());
    }
}
