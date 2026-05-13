package org.kaelth4s.castlekeeper.server.service;

import org.kaelth4s.castlekeeper.server.dto.AuthorRequest;
import org.kaelth4s.castlekeeper.server.dto.AuthorResponse;
import org.kaelth4s.castlekeeper.server.dto.AuthorTypeResponse;
import org.kaelth4s.castlekeeper.server.exception.ResourceNotFoundException;
import org.kaelth4s.castlekeeper.server.model.Author;
import org.kaelth4s.castlekeeper.server.model.AuthorType;
import org.kaelth4s.castlekeeper.server.repository.AuthorRepository;
import org.kaelth4s.castlekeeper.server.repository.AuthorTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuthorService {
    private final AuthorRepository repository;
    private final AuthorTypeRepository authorTypeRepository;

    public AuthorService(AuthorRepository repository, AuthorTypeRepository authorTypeRepository) {
        this.repository = repository;
        this.authorTypeRepository = authorTypeRepository;
    }

    public List<AuthorResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public AuthorResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
    }

    public AuthorResponse create(AuthorRequest request) {
        AuthorType type = authorTypeRepository.findById(request.getAuthorTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AuthorType not found with id: " + request.getAuthorTypeId()));

        Author entity = new Author();
        entity.setName(request.getName());
        entity.setAuthorType(type);
        return toResponse(repository.save(entity));
    }

    public AuthorResponse update(Long id, AuthorRequest request) {
        return repository.findById(id).map(existing -> {
            AuthorType type = authorTypeRepository.findById(request.getAuthorTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "AuthorType not found with id: " + request.getAuthorTypeId()));
            existing.setName(request.getName());
            existing.setAuthorType(type);
            return toResponse(repository.save(existing));
        }).orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Author not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private AuthorResponse toResponse(Author entity) {
        AuthorType type = entity.getAuthorType();
        AuthorTypeResponse typeDto = new AuthorTypeResponse(
                type.getId(), type.getName(), type.getDescription());
        return new AuthorResponse(entity.getId(), entity.getName(), typeDto);
    }
}
