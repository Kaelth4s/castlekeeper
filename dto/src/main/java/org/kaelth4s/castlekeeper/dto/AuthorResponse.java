package org.kaelth4s.castlekeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponse {
    private Long id;
    private String name;
    private AuthorTypeResponse authorType;
}
