package org.kaelth4s.castlekeeper.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotBlank(message = "Reconstruction castle must not be blank")
    @JoinColumn(name = "castle_id", nullable = false)
    private Castle castle;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotBlank(message = "Reconstruction author must not be blank")
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @Column(name = "reconstruction_year")
    private Integer reconstructionYear;
}