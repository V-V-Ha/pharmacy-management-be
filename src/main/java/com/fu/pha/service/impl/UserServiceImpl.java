package com.fu.pha.service.impl;

import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.MessageResponse;
import com.fu.pha.dto.response.JwtResponse;
import com.fu.pha.entity.Role;
import com.fu.pha.entity.User;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.UserStatus;
import com.fu.pha.exception.CustomUpdateException;
import com.fu.pha.exception.Message;
import com.fu.pha.repository.RoleRepository;
import com.fu.pha.repository.UserRepository;
import com.fu.pha.security.impl.UserDetailsImpl;
import com.fu.pha.security.jwt.JwtUtils;
import com.fu.pha.validate.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements com.fu.pha.service.UserService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    Validate validate;

    @Autowired
    JwtUtils jwtUtils;
    @Override
    public User findByUsername(String username) {
        return null;
    }

    @Override
    public ResponseEntity<JwtResponse> login(LoginDtoRequest loginDtoRequest) {
        // Xác thực thông tin đăng nhập
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDtoRequest.getUsername(), loginDtoRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        JwtResponse jwtResponse = new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);

        return ResponseEntity.ok(jwtResponse);
    }

    @Override
    public ResponseEntity<Object> createUser(UserDto request) {

        //used to validate user information
        if(!validate.validateUser(request,"create").getStatusCode().equals(HttpStatus.OK))
            return validate.validateUser(request,"create");


        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setAddress(request.getAddress());
        user.setDob(request.getDob());
        user.setGender(request.getGender());
        user.setFullName(request.getFullName());
        user.setAvatar(request.getAvatar());
        user.setPhone(request.getPhone());
        user.setCic(request.getCic());
        user.setStatus(request.getStatus());
        user.setCreateDate(Instant.now());
        user.setCreateBy(SecurityContextHolder.getContext().getAuthentication().getName());
        user.setLastModifiedDate(Instant.now());
        user.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        Set<Role> roles = request.getRolesDto().stream().map(roleDto -> {
            Role role = roleRepository.findByName(ERole.valueOf(roleDto.getName()))
                    .orElseThrow(() -> new CustomUpdateException("Error: Role is not found."));
            return role;
        }).collect(Collectors.toSet());
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse(Message.CREATE_SUCCESS, HttpStatus.CREATED.value()));
    }

    @Override
    public ResponseEntity<Object> updateUser(UserDto request) {
        //used to validate user information
        if(!validate.validateUser(request,"update").getStatusCode().equals(HttpStatus.OK))
            return validate.validateUser(request,"update");

        User user = userRepository.getUserById(request.getId());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setAddress(request.getAddress());
        user.setDob(request.getDob());
        user.setGender(request.getGender());
        user.setFullName(request.getFullName());
        user.setAvatar(request.getAvatar());
        user.setPhone(request.getPhone());
        user.setCic(request.getCic());
        user.setStatus(request.getStatus());
        user.setLastModifiedDate(Instant.now());
        user.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        Set<Role> roles = request.getRolesDto().stream().map(roleDto -> {
            Role role = roleRepository.findByName(ERole.valueOf(roleDto.getName()))
                    .orElseThrow(() -> new CustomUpdateException("Error: Role is not found."));
            return role;
        }).collect(Collectors.toSet());
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse(Message.UPDATE_SUCCESS, HttpStatus.OK.value()));
    }

    @Override
    public ResponseEntity<Object> activeUser(UserDto userDto) {

        // Find the user by ID
        User user = userRepository.getUserById(userDto.getId());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(Message.USER_NOT_FOUND, HttpStatus.NOT_FOUND.value()));
        }

        // Activate the user
        user.setStatus(UserStatus.ACTIVE);
        user.setLastModifiedDate(Instant.now());
        user.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // Save the user
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse(Message.ACTIVE_SUCCESS, HttpStatus.OK.value()));
    }

    @Override
    public ResponseEntity<Object> deActiveUser(UserDto userDto) {

        // Find the user by ID
        User user = userRepository.getUserById(userDto.getId());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(Message.USER_NOT_FOUND, HttpStatus.NOT_FOUND.value()));
        }
        // deActivate the user
        user.setStatus(UserStatus.INACTIVE);
        user.setLastModifiedDate(Instant.now());
        user.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        // Save the user
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse(Message.DEACTIVE_SUCCESS, HttpStatus.OK.value()));

    }


    //view detail user
    @Override
    public ResponseEntity<Object> viewDetailUser(Long id) {
        User user = userRepository.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(Message.USER_NOT_FOUND, HttpStatus.NOT_FOUND.value()));
        }
        UserDto userDto = new UserDto(user);
        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }

    //view all user with paging
    @Override
    public ResponseEntity<Object> getAllUserPaging(int page, int size, String fullName, String role, String status) {
        Pageable pageable = PageRequest.of(page, size);
        ERole eRole = null;
        UserStatus userStatus = null;
        if(role != null) {
            try {
                eRole = ERole.valueOf(role);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST.value()));
            }
        }

        if(status != null) {
            try {
                userStatus = UserStatus.valueOf(status);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.STATUS_NOT_FOUND, HttpStatus.BAD_REQUEST.value()));
            }
        }

        Page<UserDto> users = userRepository.getAllUserPaging(fullName,eRole, userStatus, pageable);
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(Message.USER_NOT_FOUND, HttpStatus.NOT_FOUND.value()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(users.getContent());
    }


}
