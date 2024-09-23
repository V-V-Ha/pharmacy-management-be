package com.fu.pha.service;

import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.JwtResponse;
import com.fu.pha.entity.User;
import org.springframework.http.ResponseEntity;

public interface UserService {
    User findByUsername(String username);

    ResponseEntity<JwtResponse> login(LoginDtoRequest loginDtoRequest);
    ResponseEntity<Object> register(UserDto request, String token);
}
