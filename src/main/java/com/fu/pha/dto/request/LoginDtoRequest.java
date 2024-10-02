package com.fu.pha.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDtoRequest {
    private String username;
    private String password;
}
