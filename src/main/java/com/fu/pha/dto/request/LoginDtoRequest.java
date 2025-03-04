package com.fu.pha.dto.request;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDtoRequest {
    private String username;
    private String password;
    private String fcmToken;
}
