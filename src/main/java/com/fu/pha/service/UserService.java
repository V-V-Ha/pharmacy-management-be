package com.fu.pha.service;

import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.dto.response.JwtResponse;
import com.fu.pha.dto.response.ServiceResponse;
import com.fu.pha.entity.User;

public interface UserService {
    User findByUsername(String username);

    ServiceResponse<JwtResponse> login(LoginDtoRequest loginDtoRequest);
//    ServiceResponse<Object> register(UserDto request, String token);
}
