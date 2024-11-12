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
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    @PutMapping("/in-active-user")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<String> deActiveUser(@RequestBody UserDto request) {
         userService.deActiveUser(request);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    @GetMapping("/get-all-user-paging")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<PageResponseModel<UserDto>> getAllUserPaging(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(required = false) String fullName,
                                                   @RequestParam(required = false) String role,
                                                   @RequestParam(required = false) String status) {

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

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(
            @RequestPart("userDto") UserDto userDto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
            // Gọi service để tạo user và upload avatar
            userService.createUser(userDto, file);
            return ResponseEntity.ok(Message.CREATE_SUCCESS);

    }

    @PutMapping("/update-user")
    public ResponseEntity<?> updateUser(
            @RequestPart("userDto") UserDto userDto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

            // Gọi service để cập nhật user và upload avatar
            userService.updateUser(userDto, file);
            return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }
}