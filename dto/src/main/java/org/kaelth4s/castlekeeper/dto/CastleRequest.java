package org.kaelth4s.castlekeeper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CastleRequest {
    @NotBlank(message = "Castle name must not be blank")
    @Size(max = 255)
    private String name;

    @Size(max = 5000)
    private String description;

    private Long authorId;
    private Integer builtYear;
    private Integer destroyedYear;
    private BigDecimal heightM;
    private Long materialId;
}
