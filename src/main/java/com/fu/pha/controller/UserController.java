package com.fu.pha.controller;

import com.fu.pha.dto.request.ChangePasswordDto;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;
    @PutMapping("/active-user")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<Object> activeUser(@RequestBody UserDto request) {
        return userService.activeUser(request);
    }

    @PutMapping("/in-active-user")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<Object> deActiveUser(@RequestBody UserDto request) {
        return userService.deActiveUser(request);
    }

    @GetMapping("/get-all-user-paging")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<Object> getAllUserPaging(@RequestParam int page,
                                                   @RequestParam(required = false) String fullName,
                                                   @RequestParam(required = false) String role,
                                                   @RequestParam(required = false) String status) {
        int size = 10;
        Page<UserDto> content = userService.getAllUserPaging(page, size, fullName, role, status);

        List<UserDto> listData = content.getContent();

        PageResponseModel<UserDto> response = PageResponseModel.<UserDto>builder()
                .page(page)
                .size(size)
                .total(content.getTotalElements())
                .listData(listData)
                .build();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/view-detail-user")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<Object> viewDetailUser(@RequestParam Long id) {
        return userService.viewDetailUser(id);
    }


    @PutMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody ChangePasswordDto request, @RequestParam String token) {
        return userService.resetPassword(request, token);
    }

    //upload avatar
    @PostMapping("/upload-avatar")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<Object> uploadImage(@RequestParam final Long id,@RequestParam("file") MultipartFile file) {
        return userService.uploadImage(id ,file);
    }


    @PostMapping("/create-user")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<Object> createUser(@RequestBody UserDto request) {
        return userService.createUser(request);
    }

    @PutMapping("/update-user")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<Object> updateUser(@RequestBody UserDto request) {
        return userService.updateUser(request);
    }



}

