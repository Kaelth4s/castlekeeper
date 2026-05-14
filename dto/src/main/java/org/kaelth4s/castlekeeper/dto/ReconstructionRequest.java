package org.kaelth4s.castlekeeper.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReconstructionRequest {
    @NotNull(message = "Castle ID is required")
    private Long castleId;

    @NotNull(message = "Author ID is required")
    private Long authorId;

    private Integer reconstructionYear;
}
