package org.kaelth4s.castlekeeper.server.service;

import org.kaelth4s.castlekeeper.server.exception.ResourceNotFoundException;
import org.kaelth4s.castlekeeper.server.model.AuthorType;
import org.kaelth4s.castlekeeper.server.repository.AuthorTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuthorTypeService {
    private final AuthorTypeRepository repository;

    public AuthorTypeService(AuthorTypeRepository repository) {
        this.repository = repository;
    }

    public List<AuthorType> getAll() {
        return repository.findAll();
    }

    public Optional<AuthorType> getById(Long id) {
        return repository.findById(id);
    }

    public AuthorType create(AuthorType authorType) {
        authorType.setId(null);
        return repository.save(authorType);
    }

    public AuthorType update(Long id, AuthorType updated) {
        return repository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            return repository.save(existing);
        }).orElseThrow(() -> new ResourceNotFoundException("AuthorType not found with id: " + id));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("AuthorType not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
