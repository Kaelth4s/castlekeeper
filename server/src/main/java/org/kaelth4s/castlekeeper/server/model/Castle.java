package org.kaelth4s.castlekeeper.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "castle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Castle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    @Column(name = "built_year")
    private Integer builtYear;

    @Column(name = "destroyed_year")
    private Integer destroyedYear;

    @Column(name = "height_m")
    private BigDecimal heightM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;
}
