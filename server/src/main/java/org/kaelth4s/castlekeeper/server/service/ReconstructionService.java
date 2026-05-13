package org.kaelth4s.castlekeeper.server.service;

import org.kaelth4s.castlekeeper.server.dto.*;
import org.kaelth4s.castlekeeper.server.exception.ResourceNotFoundException;
import org.kaelth4s.castlekeeper.server.model.*;
import org.kaelth4s.castlekeeper.server.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReconstructionService {
    private final ReconstructionRepository repository;
    private final CastleRepository castleRepository;
    private final AuthorRepository authorRepository;

    public ReconstructionService(ReconstructionRepository repository,
                                  CastleRepository castleRepository,
                                  AuthorRepository authorRepository) {
        this.repository = repository;
        this.castleRepository = castleRepository;
        this.authorRepository = authorRepository;
    }

    public List<ReconstructionResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public ReconstructionResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Reconstruction not found with id: " + id));
    }

    public ReconstructionResponse create(ReconstructionRequest request) {
        Reconstruction entity = new Reconstruction();
        entity.setReconstructionYear(request.getReconstructionYear());
        resolveFks(entity, request);
        return toResponse(repository.save(entity));
    }

    public ReconstructionResponse update(Long id, ReconstructionRequest request) {
        return repository.findById(id).map(existing -> {
            existing.setReconstructionYear(request.getReconstructionYear());
            resolveFks(existing, request);
            return toResponse(repository.save(existing));
        }).orElseThrow(() -> new ResourceNotFoundException("Reconstruction not found with id: " + id));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Reconstruction not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private void resolveFks(Reconstruction entity, ReconstructionRequest request) {
        Castle castle = castleRepository.findById(request.getCastleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Castle not found with id: " + request.getCastleId()));
        entity.setCastle(castle);

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Author not found with id: " + request.getAuthorId()));
        entity.setAuthor(author);
    }

    private ReconstructionResponse toResponse(Reconstruction entity) {
        ReconstructionResponse dto = new ReconstructionResponse();
        dto.setId(entity.getId());
        dto.setReconstructionYear(entity.getReconstructionYear());

        // Castle → CastleResponse (with nested author/material)
        Castle castle = entity.getCastle();
        CastleResponse castleDto = new CastleResponse();
        castleDto.setId(castle.getId());
        castleDto.setName(castle.getName());
        // Minimal nested — full depth would need recursive mapping
        if (castle.getAuthor() != null) {
            Author ca = castle.getAuthor();
            AuthorTypeResponse typeDto = ca.getAuthorType() != null
                    ? new AuthorTypeResponse(ca.getAuthorType().getId(), ca.getAuthorType().getName(),
                                             ca.getAuthorType().getDescription())
                    : null;
            castleDto.setAuthor(new AuthorResponse(ca.getId(), ca.getName(), typeDto));
        }
        if (castle.getMaterial() != null) {
            Material cm = castle.getMaterial();
            castleDto.setMaterial(new MaterialResponse(cm.getId(), cm.getName()));
        }
        dto.setCastle(castleDto);

        // Author → AuthorResponse
        Author authorEntity = entity.getAuthor();
        AuthorTypeResponse typeDto = authorEntity.getAuthorType() != null
                ? new AuthorTypeResponse(authorEntity.getAuthorType().getId(),
                                         authorEntity.getAuthorType().getName(),
                                         authorEntity.getAuthorType().getDescription())
                : null;
        dto.setAuthor(new AuthorResponse(authorEntity.getId(), authorEntity.getName(), typeDto));

        return dto;
    }
}
