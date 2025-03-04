package com.fu.pha.controller;

import com.fu.pha.dto.request.ChangePasswordDto;
import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.JwtResponse;
import com.fu.pha.exception.Message;
import com.fu.pha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    UserService userService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginDtoRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
        return ResponseEntity.ok(Message.FORGOT_PASS_SUCCESS);
    }




}
