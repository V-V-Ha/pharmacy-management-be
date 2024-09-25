package com.fu.pha.controller;

import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.JwtResponse;
import com.fu.pha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*" , maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    UserService userService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginDtoRequest request) {
        return userService.login(request);
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<Object> createUser(@RequestBody UserDto request) {
        return userService.createUser(request);
    }

    @PostMapping("/update-user")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<Object> updateUser(@RequestBody UserDto request) {
        return userService.updateUser(request);
    }



}
