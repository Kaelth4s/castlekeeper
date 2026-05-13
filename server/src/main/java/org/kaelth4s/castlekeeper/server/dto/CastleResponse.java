package org.kaelth4s.castlekeeper.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CastleResponse {
    private Long id;
    private String name;
    private String description;
    private AuthorResponse author;
    private Integer builtYear;
    private Integer destroyedYear;
    private BigDecimal heightM;
    private MaterialResponse material;
}
