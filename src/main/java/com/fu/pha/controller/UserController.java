package com.fu.pha.controller;

import com.fu.pha.dto.request.UserDto;
import com.fu.pha.enums.ERole;
import com.fu.pha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;
    @PostMapping("/active-user")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<Object> activeUser(@RequestBody UserDto request) {
        return userService.activeUser(request);
    }

    @PostMapping("/de-active-user")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<Object> deActiveUser(@RequestBody UserDto request) {
        return userService.deActiveUser(request);
    }

    @GetMapping("/get-all-user-paging")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<Object> getAllUserPaging(@RequestParam int page,
                                                   @RequestParam int size,
                                                   @RequestParam(required = false) String fullName,
                                                   @RequestParam(required = false) String role,
                                                   @RequestParam(required = false) String status) {
        return userService.getAllUserPaging(page, size, fullName, role, status);
    }

    @GetMapping("/view-detail-user")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public ResponseEntity<Object> viewDetailUser(@RequestParam Long id) {
        return userService.viewDetailUser(id);
    }

}

