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

    JwtResponse login(LoginDtoRequest loginDtoRequest);
    void createUser(UserDto request);

    void updateUser(UserDto request);

    void activeUser(UserDto userDto);
    void deActiveUser(UserDto userDto);

    UserDto viewDetailUser(Long id);
    // get all user and paging
    Page<UserDto> getAllUserPaging(int page, int size, String fullName, String role, String status);

    //forgot password and send email
    ResponseEntity<Object> forgotPassword(String email);

    //reset password
    void resetPassword(ChangePasswordDto request, String token);

    void uploadImage(final Long id, final MultipartFile file);



}
