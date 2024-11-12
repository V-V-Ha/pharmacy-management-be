package com.fu.pha.controller;

import com.fu.pha.dto.request.ChangePasswordDto;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.exception.Message;
import com.fu.pha.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testActiveUser() {
        UserDto userDto = new UserDto();
        doNothing().when(userService).activeUser(any(UserDto.class));

        ResponseEntity<String> response = userController.activeUser(userDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Message.UPDATE_SUCCESS, response.getBody());
    }

    @Test
    void testDeActiveUser() {
        UserDto userDto = new UserDto();
        doNothing().when(userService).deActiveUser(any(UserDto.class));

        ResponseEntity<String> response = userController.deActiveUser(userDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Message.UPDATE_SUCCESS, response.getBody());
    }

    @Test
    void testGetAllUserPaging() {
        Page<UserDto> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(userService.getAllUserPaging(anyInt(), anyInt(), anyString(), anyString(), anyString())).thenReturn(page);

        ResponseEntity<PageResponseModel<UserDto>> response = userController.getAllUserPaging(0, 10, "", "", "");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, Objects.requireNonNull(response.getBody()).getTotal());
    }

    @Test
    void testViewDetailUser() {
        UserDto userDto = new UserDto();
        when(userService.viewDetailUser(anyLong())).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.viewDetailUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto, response.getBody());
    }

    @Test
    void testResetPassword() {
        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        doNothing().when(userService).resetPassword(any(ChangePasswordDto.class), anyString());

        ResponseEntity<String> response = userController.resetPassword(changePasswordDto, "token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Message.CHANGE_PASS_SUCCESS, response.getBody());
    }

    @Test
    void testCreateUser() {
        UserDto userDto = new UserDto();
        MultipartFile file = mock(MultipartFile.class);
        doNothing().when(userService).createUser(any(UserDto.class), any(MultipartFile.class));

        ResponseEntity<?> response = userController.createUser(userDto, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Message.CREATE_SUCCESS, response.getBody());
    }

    @Test
    void testUpdateUser() {
        UserDto userDto = new UserDto();
        MultipartFile file = mock(MultipartFile.class);
        doNothing().when(userService).updateUser(any(UserDto.class), any(MultipartFile.class));

        ResponseEntity<?> response = userController.updateUser(userDto, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Message.UPDATE_SUCCESS, response.getBody());
    }


}