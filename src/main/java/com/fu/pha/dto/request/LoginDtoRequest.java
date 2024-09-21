package com.fu.pha.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDtoRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;

}
