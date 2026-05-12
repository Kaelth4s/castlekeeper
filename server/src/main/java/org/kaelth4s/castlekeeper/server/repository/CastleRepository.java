package org.kaelth4s.castlekeeper.server.repository;

import org.kaelth4s.castlekeeper.server.model.Castle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CastleRepository extends JpaRepository<Castle, Long> {
    @Query(value = "SELECT * FROM castle ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Castle> findRandom();
}