package org.kaelth4s.castlekeeper.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorTypeResponse {
    private Long id;
    private String name;
    private String description;
}
