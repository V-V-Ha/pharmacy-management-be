package com.fu.pha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String fcmToken;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private List<String> roles;

    public JwtResponse(String token,String fcmToken, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.fcmToken = fcmToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}
