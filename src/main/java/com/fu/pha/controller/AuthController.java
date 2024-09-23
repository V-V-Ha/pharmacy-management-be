package com.fu.pha.controller;

import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.dto.response.JwtResponse;
import com.fu.pha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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



}
