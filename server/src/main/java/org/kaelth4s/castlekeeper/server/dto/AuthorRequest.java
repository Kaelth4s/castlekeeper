package org.kaelth4s.castlekeeper.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorRequest {
    @NotBlank(message = "Author name must not be blank")
    @Size(max = 255)
    private String name;

    @NotNull(message = "Author type ID is required")
    private Long authorTypeId;
}
