package org.kaelth4s.castlekeeper.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReconstructionResponse {
    private Long id;
    private CastleResponse castle;
    private AuthorResponse author;
    private Integer reconstructionYear;
}
