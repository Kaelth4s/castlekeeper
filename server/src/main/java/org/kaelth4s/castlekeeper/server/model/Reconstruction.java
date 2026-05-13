package org.kaelth4s.castlekeeper.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reconstruction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reconstruction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "castle_id", nullable = false)
    private Castle castle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @Column(name = "reconstruction_year")
    private Integer reconstructionYear;
}
