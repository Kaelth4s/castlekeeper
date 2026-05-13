package org.kaelth4s.castlekeeper.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorTypeRequest {
    @NotBlank(message = "Author type name must not be blank")
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;
}
