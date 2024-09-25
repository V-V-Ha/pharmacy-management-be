package com.fu.pha.service;

import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.JwtResponse;
import com.fu.pha.entity.User;
import com.fu.pha.enums.ERole;
import org.springframework.http.ResponseEntity;

public interface UserService {
    User findByUsername(String username);

    ResponseEntity<JwtResponse> login(LoginDtoRequest loginDtoRequest);
    ResponseEntity<Object> createUser(UserDto request);

    ResponseEntity<Object> updateUser(UserDto request);

    ResponseEntity<Object> activeUser(UserDto userDto);
    ResponseEntity<Object> deActiveUser(UserDto userDto);

    ResponseEntity<Object> viewDetailUser(Long id);
    // get all user and paging
    ResponseEntity<Object> getAllUserPaging(int page, int size, String fullName, String role, String status);



}
