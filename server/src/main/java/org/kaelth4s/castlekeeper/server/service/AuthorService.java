package org.kaelth4s.castlekeeper.server.service;

import org.kaelth4s.castlekeeper.server.exception.ResourceNotFoundException;
import org.kaelth4s.castlekeeper.server.model.Author;
import org.kaelth4s.castlekeeper.server.repository.AuthorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuthorService {
    private final AuthorRepository repository;

    public AuthorService(AuthorRepository repository) {
        this.repository = repository;
    }

    public List<Author> getAll() {
        return repository.findAll();
    }

    public Optional<Author> getById(Long id) {
        return repository.findById(id);
    }

    public Author create(Author author) {
        author.setId(null);
        return repository.save(author);
    }

    public Author update(Long id, Author updated) {
        return repository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setAuthorType(updated.getAuthorType());
            return repository.save(existing);
        }).orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Author not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
