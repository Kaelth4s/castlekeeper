package org.kaelth4s.castlekeeper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequest {
    @NotBlank(message = "Material name must not be blank")
    @Size(max = 255)
    private String name;
}
