package com.fu.pha.controller;

import com.fu.pha.dto.request.ChangePasswordDto;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.exception.Message;
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
    public ResponseEntity<String> activeUser(@RequestBody UserDto request) {
        userService.activeUser(request);
        return ResponseEntity.ok(Message.ACTIVE_SUCCESS);
    }

    @PutMapping("/in-active-user")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<String> deActiveUser(@RequestBody UserDto request) {
         userService.deActiveUser(request);
        return ResponseEntity.ok(Message.DEACTIVE_SUCCESS);
    }

    @GetMapping("/get-all-user-paging")
//    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<PageResponseModel<UserDto>> getAllUserPaging(@RequestParam int page,
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
    public ResponseEntity<UserDto> viewDetailUser(@RequestParam Long id) {
        return ResponseEntity.ok(userService.viewDetailUser(id));
    }


    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ChangePasswordDto request, @RequestParam String token) {
        userService.resetPassword(request, token);
        return ResponseEntity.ok(Message.CHANGE_PASS_SUCCESS);
    }

    //upload avatar
    @PostMapping("/upload-avatar")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<String> uploadImage(@RequestParam final Long id,@RequestParam("file") MultipartFile file) {
        userService.uploadImage(id, file);
        return ResponseEntity.ok(Message.UPLOAD_SUCCESS);
    }


    @PostMapping("/create-user")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<String> createUser(@RequestBody UserDto request) {
        userService.createUser(request);
        return ResponseEntity.ok(Message.CREATE_SUCCESS);
    }

    @PutMapping("/update-user")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<String> updateUser(@RequestBody UserDto request) {
        userService.updateUser(request);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }
}

