package com.fu.pha.controller;

import com.fu.pha.dto.request.AccountCreationRequest;
import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.dto.response.JwtResponse;
import com.fu.pha.dto.response.MessageResponse;
import com.fu.pha.entity.Role;
import com.fu.pha.enums.ERole;
import com.fu.pha.entity.User;
import com.fu.pha.repository.RoleRepository;
import com.fu.pha.repository.UserRepository;
import com.fu.pha.security.impl.UserDetailsImpl;
import com.fu.pha.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@CrossOrigin(origins = "*" , maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDtoRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/create-account")
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountCreationRequest request) {
        // Kiểm tra nếu tên người dùng đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Tên đăng nhập đã tồn tại!"));
        }

        // Kiểm tra nếu email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email đã tồn tại!"));
        }

        // Tạo tài khoản người dùng mới
        User user = new User(request.getUsername(),
                request.getEmail(),
                encoder.encode(request.getPassword()));

        // Thiết lập vai trò cho người dùng
        Set<String> strRoles = request.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role defaultRole = roleRepository.findByName(ERole.ROLE_SALE)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(defaultRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "ROLE_PRODUCT_OWNER":
                        Role productOwnerRole = roleRepository.findByName(ERole.ROLE_PRODUCT_OWNER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(productOwnerRole);

                        break;
                    case "ROLE_STOCK":
                        Role stockRole = roleRepository.findByName(ERole.ROLE_STOCK)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(stockRole);

                        break;
                    default:
                        Role saleRole = roleRepository.findByName(ERole.ROLE_SALE)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(saleRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Tài khoản được tạo thành công!"));
    }



}
