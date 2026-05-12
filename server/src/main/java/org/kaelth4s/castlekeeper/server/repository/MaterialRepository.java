package org.kaelth4s.castlekeeper.server.repository;

import org.kaelth4s.castlekeeper.server.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {}
