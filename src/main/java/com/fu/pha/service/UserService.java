package com.fu.pha.service;

import com.fu.pha.dto.request.ChangePasswordDto;
import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.JwtResponse;
import com.fu.pha.entity.User;
import com.fu.pha.enums.ERole;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    User findByUsername(String username);

    ResponseEntity<JwtResponse> login(LoginDtoRequest loginDtoRequest);
    ResponseEntity<Object> createUser(UserDto request);

    ResponseEntity<Object> updateUser(UserDto request);

    ResponseEntity<Object> activeUser(UserDto userDto);
    ResponseEntity<Object> deActiveUser(UserDto userDto);

    ResponseEntity<Object> viewDetailUser(Long id);
    // get all user and paging
    Page<UserDto> getAllUserPaging(int page, int size, String fullName, String role, String status);

    //forgot password and send email
    ResponseEntity<Object> forgotPassword(String email);

    //reset password
    ResponseEntity<Object> resetPassword(ChangePasswordDto request, String token);

    ResponseEntity<Object> uploadImage(final Long id, final MultipartFile file);



}
