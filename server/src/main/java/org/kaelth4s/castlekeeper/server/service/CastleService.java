package org.kaelth4s.castlekeeper.server.service;

import org.kaelth4s.castlekeeper.server.dto.*;
import org.kaelth4s.castlekeeper.server.exception.ResourceNotFoundException;
import org.kaelth4s.castlekeeper.server.model.*;
import org.kaelth4s.castlekeeper.server.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CastleService {
    private final CastleRepository repository;
    private final AuthorRepository authorRepository;
    private final MaterialRepository materialRepository;

    public CastleService(CastleRepository repository,
                         AuthorRepository authorRepository,
                         MaterialRepository materialRepository) {
        this.repository = repository;
        this.authorRepository = authorRepository;
        this.materialRepository = materialRepository;
    }

    public List<CastleResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public CastleResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Castle not found with id: " + id));
    }

    public Optional<CastleResponse> getRandom() {
        return repository.findRandom().map(this::toResponse);
    }

    public CastleResponse create(CastleRequest request) {
        Castle entity = new Castle();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setBuiltYear(request.getBuiltYear());
        entity.setDestroyedYear(request.getDestroyedYear());
        entity.setHeightM(request.getHeightM());
        resolveFks(entity, request);
        return toResponse(repository.save(entity));
    }

    public CastleResponse update(Long id, CastleRequest request) {
        return repository.findById(id).map(existing -> {
            existing.setName(request.getName());
            existing.setDescription(request.getDescription());
            existing.setBuiltYear(request.getBuiltYear());
            existing.setDestroyedYear(request.getDestroyedYear());
            existing.setHeightM(request.getHeightM());
            resolveFks(existing, request);
            return toResponse(repository.save(existing));
        }).orElseThrow(() -> new ResourceNotFoundException("Castle not found with id: " + id));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Castle not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private void resolveFks(Castle entity, CastleRequest request) {
        if (request.getAuthorId() != null) {
            Author author = authorRepository.findById(request.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Author not found with id: " + request.getAuthorId()));
            entity.setAuthor(author);
        }
        if (request.getMaterialId() != null) {
            Material material = materialRepository.findById(request.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Material not found with id: " + request.getMaterialId()));
            entity.setMaterial(material);
        }
    }

    private CastleResponse toResponse(Castle entity) {
        CastleResponse dto = new CastleResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setBuiltYear(entity.getBuiltYear());
        dto.setDestroyedYear(entity.getDestroyedYear());
        dto.setHeightM(entity.getHeightM());

        if (entity.getAuthor() != null) {
            AuthorType type = entity.getAuthor().getAuthorType();
            AuthorTypeResponse typeDto = type != null
                    ? new AuthorTypeResponse(type.getId(), type.getName(), type.getDescription())
                    : null;
            Author author = entity.getAuthor();
            dto.setAuthor(new AuthorResponse(author.getId(), author.getName(), typeDto));
        }

        if (entity.getMaterial() != null) {
            Material mat = entity.getMaterial();
            dto.setMaterial(new MaterialResponse(mat.getId(), mat.getName()));
        }

        return dto;
    }
}
