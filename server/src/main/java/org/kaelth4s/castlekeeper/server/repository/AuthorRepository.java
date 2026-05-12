package org.kaelth4s.castlekeeper.server.repository;

import org.kaelth4s.castlekeeper.server.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {}