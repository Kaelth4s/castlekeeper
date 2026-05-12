package org.kaelth4s.castlekeeper.server.service;

import org.kaelth4s.castlekeeper.server.model.Reconstruction;
import org.kaelth4s.castlekeeper.server.repository.ReconstructionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReconstructionService {
    private final ReconstructionRepository repository;

    public ReconstructionService(ReconstructionRepository repository) {
        this.repository = repository;
    }

    public List<Reconstruction> getAll() {
        return repository.findAll();
    }

    public Optional<Reconstruction> getById(Long id) {
        return repository.findById(id);
    }

    public Reconstruction create(Reconstruction reconstruction) {
        return repository.save(reconstruction);
    }

    public Reconstruction update(Long id, Reconstruction updated) {
        return repository.findById(id).map(existing -> {
            existing.setCastle(updated.getCastle());
            existing.setAuthor(updated.getAuthor());
            existing.setReconstructionYear(updated.getReconstructionYear());
            return repository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Reconstruction not found: " + id));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
