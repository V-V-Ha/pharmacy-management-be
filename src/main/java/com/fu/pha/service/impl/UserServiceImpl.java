package com.fu.pha.service.impl;

import com.fu.pha.dto.request.ChangePasswordDto;
import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.CloudinaryResponse;
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
import com.fu.pha.service.CloudinaryService;
import com.fu.pha.util.FileUploadUtil;
import com.fu.pha.validate.Constants;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
    CloudinaryService cloudinaryService;

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
    public Page<UserDto> getAllUserPaging(int page, int size, String fullName, String role, String status) {
        Pageable pageable = PageRequest.of(page, size);
        ERole eRole = null;
        UserStatus userStatus = null;
        if (role != null) {
            try {
                eRole = ERole.valueOf(role);
            } catch (Exception e) {
                throw new CustomUpdateException(Message.ROLE_NOT_FOUND);
            }
        }

        if (status != null) {
            try {
                userStatus = UserStatus.valueOf(status);
            } catch (Exception e) {
                throw new CustomUpdateException(Message.STATUS_NOT_FOUND);
            }
        }

        Page<UserDto> users = userRepository.getAllUserPaging(fullName, eRole, userStatus, pageable);
        if (users.isEmpty()) {
            throw new CustomUpdateException(Message.USER_NOT_FOUND);
        }
        return users;
    }

    @Override
    public ResponseEntity<Object> forgotPassword(String email) {

        return null;
    }

    @Override
    public ResponseEntity<Object> resetPassword(ChangePasswordDto request, String token) {
        // Validate the token
        if (!jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(Message.INVALID_TOKEN, HttpStatus.UNAUTHORIZED.value()));
        }

        // Get the username from the token
        String username = jwtUtils.getUserNameFromJwtToken(token);

        // Find the user by username
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(Message.USER_NOT_FOUND, HttpStatus.NOT_FOUND.value()));
        }
        //check null field
        if (request.getNewPassword() == null || request.getConfirmPassword() == null || request.getOldPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.NULL_FILED, HttpStatus.BAD_REQUEST.value()));
        }

        //check Old password
        if (!encoder.matches(request.getOldPassword(), user.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.INVALID_OLD_PASSWORD, HttpStatus.BAD_REQUEST.value()));
        }

        // check new password match Regex
        if (!request.getNewPassword().matches(Constants.REGEX_PASSWORD)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.INVALID_PASSWORD, HttpStatus.BAD_REQUEST.value()));
        }

        // Validate the new password and confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.NOT_MATCH_PASSWORD, HttpStatus.BAD_REQUEST.value()));
        }

        // Update the user's password
        user.get().setPassword(encoder.encode(request.getNewPassword()));
        user.get().setLastModifiedDate(Instant.now());
        user.get().setLastModifiedBy(username);
        userRepository.save(user.get());

        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse(Message.CHANGE_PASS_SUCCESS, HttpStatus.OK.value()));
    }
    @Override
    public ResponseEntity<Object> uploadImage(final Long id ,final MultipartFile file) {
        // Check if the file is empty
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(Message.EMPTY_FILE, HttpStatus.NOT_FOUND.value()));
        }

        // Check if the file is an image
        if (!FileUploadUtil.isAllowedExtension(file.getOriginalFilename(), FileUploadUtil.IMAGE_PATTERN)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.INVALID_FILE, HttpStatus.BAD_REQUEST.value()));
        }

        // Check if the file size is greater than 2MB
        if (file.getSize() > FileUploadUtil.MAX_FILE_SIZE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.INVALID_FILE_SIZE, HttpStatus.BAD_REQUEST.value()));
        }
        User user = userRepository.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(Message.USER_NOT_FOUND, HttpStatus.NOT_FOUND.value()));
        }
        String customFileName = user.getFullName() + "avatar";

        // Upload the image to Cloudinary
        CloudinaryResponse cloudinaryResponse = cloudinaryService.upLoadFile(file, FileUploadUtil.getFileName(customFileName));

        // Save the image URL to the database
        user.setAvatar(cloudinaryResponse.getUrl());
        user.setLastModifiedBy(user.getFullName());
        user.setLastModifiedDate(Instant.now());
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse(Message.UPLOAD_SUCCESS, HttpStatus.OK.value()));
    }
}
