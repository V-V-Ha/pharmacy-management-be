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
import com.fu.pha.enums.Status;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
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
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
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
    public JwtResponse login(LoginDtoRequest loginDtoRequest) {
        //check username and password
        if (loginDtoRequest.getUsername() == null || loginDtoRequest.getUsername().trim().isEmpty()) {
            throw new ResourceNotFoundException(Message.MUST_FILL_USERNAME);
        }
        if (loginDtoRequest.getPassword() == null || loginDtoRequest.getPassword().trim().isEmpty()) {
            throw new ResourceNotFoundException(Message.MUST_FILL_PASSWORD);
        }
        // Xác thực thông tin đăng nhập
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDtoRequest.getUsername(), loginDtoRequest.getPassword()));


        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);

    }

    @Override
    public void createUser(UserDto userDto) {

        checkValidate(userDto);

        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new BadRequestException(Message.EXIST_USERNAME);
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new BadRequestException(Message.EXIST_EMAIL);
        }
        if (userRepository.getUserByCic(userDto.getCic()) != null) {
            throw new BadRequestException(Message.EXIST_CCCD);
        }
        if (userRepository.getUserByPhone(userDto.getPhone()) != null) {
            throw new BadRequestException(Message.EXIST_PHONE);
        }


        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(encoder.encode(userDto.getPassword()));
        user.setAddress(userDto.getAddress());
        user.setDob(userDto.getDob());
        user.setGender(userDto.getGender());
        user.setFullName(userDto.getFullName());
        user.setAvatar(userDto.getAvatar());
        user.setPhone(userDto.getPhone());
        user.setCic(userDto.getCic());
        user.setStatus(userDto.getStatus());
        user.setNote(userDto.getNote());
        user.setCreateDate(Instant.now());
        user.setCreateBy(SecurityContextHolder.getContext().getAuthentication().getName());
        user.setLastModifiedDate(Instant.now());
        user.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        Set<Role> roles = userDto.getRolesDto().stream().map(roleDto -> {
            Role role = roleRepository.findByName(ERole.valueOf(roleDto.getName()))
                    .orElseThrow(() -> new ResourceNotFoundException(Message.ROLE_NOT_FOUND));
            return role;
        }).collect(Collectors.toSet());
        user.setRoles(roles);
        userRepository.save(user);
    }

    @Override
    public void updateUser(UserDto userDto) {
        //used to validate user information
        checkValidate(userDto);

        User user = userRepository.getUserById(userDto.getId());
        if (user == null) {
            throw new ResourceNotFoundException(Message.USER_NOT_FOUND);
        }
        User emailUser = userRepository.getUserByEmail(userDto.getEmail());
        User phoneUser = userRepository.getUserByPhone(userDto.getPhone());
        User cicUser = userRepository.getUserByCic(userDto.getCic());
        Optional<User> usernameUser = userRepository.findByUsername(userDto.getUsername());

        if (emailUser != null && !emailUser.equals(user)) {
            throw new BadRequestException(Message.EXIST_EMAIL);
        }
        if (phoneUser != null && !phoneUser.equals(user)) {
            throw new BadRequestException(Message.EXIST_PHONE);
        }
        if (cicUser != null && !cicUser.equals(user)) {
            throw new BadRequestException(Message.EXIST_CCCD);
        }
        if (usernameUser.isPresent() && !usernameUser.get().equals(user)) {
            throw new BadRequestException(Message.EXIST_USERNAME);
        }

        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setAddress(userDto.getAddress());
        user.setDob(userDto.getDob());
        user.setGender(userDto.getGender());
        user.setFullName(userDto.getFullName());
        user.setAvatar(userDto.getAvatar());
        user.setPhone(userDto.getPhone());
        user.setCic(userDto.getCic());
        user.setStatus(userDto.getStatus());
        user.setNote(userDto.getNote());
        user.setLastModifiedDate(Instant.now());
        user.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        Set<Role> roles = userDto.getRolesDto().stream().map(roleDto -> {
            Role role = roleRepository.findByName(ERole.valueOf(roleDto.getName()))
                    .orElseThrow(() -> new ResourceNotFoundException(Message.ROLE_NOT_FOUND));
            return role;
        }).collect(Collectors.toSet());
        user.setRoles(roles);
        userRepository.save(user);
    }

    private void checkValidate(UserDto userDto) {
        if (userDto.getFullName() == null || userDto.getEmail() == null || userDto.getPhone() == null ||
                userDto.getDob() == null || userDto.getAddress() == null ||
                userDto.getGender() == null || userDto.getCic() == null || userDto.getUsername() == null ||
                userDto.getPassword() == null || userDto.getRolesDto() == null || userDto.getStatus() == null) {
            throw new BadRequestException(Message.NULL_FILED);
        }
        if (!checkUserAge(userDto)) {
            throw new BadRequestException(Message.INVALID_AGE);
        }
        if (!userDto.getFullName().matches(Constants.REGEX_NAME)) {
            throw new BadRequestException(Message.INVALID_NAME);
        }
        if (!userDto.getEmail().matches(Constants.REGEX_GMAIL)) {
            throw new BadRequestException(Message.INVALID_GMAIL);
        }
        if (!userDto.getPhone().matches(Constants.REGEX_PHONE)) {
            throw new BadRequestException(Message.INVALID_PHONE);
        }
        if (!userDto.getAddress().matches(Constants.REGEX_ADDRESS)) {
            throw new BadRequestException(Message.INVALID_ADDRESS);
        }
        if (!userDto.getCic().matches(Constants.REGEX_CCCD)) {
            throw new BadRequestException(Message.INVALID_CCCD);
        }
        if (!userDto.getUsername().matches(Constants.REGEX_USER_NAME)) {
            throw new BadRequestException(Message.INVALID_USERNAME_C);
        }
        if (!userDto.getPassword().matches(Constants.REGEX_PASSWORD)) {
            throw new BadRequestException(Message.INVALID_PASSWORD);
        }
    }

    @Override
    public void activeUser(UserDto userDto) {

        // Find the user by ID
        User user = userRepository.getUserById(userDto.getId());
        if (user == null) {
            throw new ResourceNotFoundException(Message.USER_NOT_FOUND);
        }

        // Activate the user
        user.setStatus(Status.ACTIVE);
        user.setLastModifiedDate(Instant.now());
        user.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // Save the user
        userRepository.save(user);
    }

    @Override
    public void deActiveUser(UserDto userDto) {

        // Find the user by ID
        User user = userRepository.getUserById(userDto.getId());
        if (user == null) {
            throw new ResourceNotFoundException(Message.USER_NOT_FOUND);
        }
        // deActivate the user
        user.setStatus(Status.INACTIVE);
        user.setLastModifiedDate(Instant.now());
        user.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        // Save the user
        userRepository.save(user);
    }


    //view detail user
    @Override
    public UserDto viewDetailUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));
        return new UserDto(user);
    }

    //view all user with paging
    @Override
    public Page<UserDto> getAllUserPaging(int page, int size, String fullName, String role, String status) {
        Pageable pageable = PageRequest.of(page, size);
        ERole eRole = null;
        Status userStatus = null;
        if (role != null) {
            try {
                eRole = ERole.valueOf(role);
            } catch (Exception e) {
                throw new ResourceNotFoundException(Message.ROLE_NOT_FOUND);
            }
        }

        if (status != null) {
            try {
                userStatus = Status.valueOf(status);
            } catch (Exception e) {
                throw new ResourceNotFoundException(Message.STATUS_NOT_FOUND);
            }
        }

        Page<UserDto> users = userRepository.getAllUserPaging(fullName, eRole, userStatus, pageable);
        if (users.isEmpty()) {
            throw new ResourceNotFoundException(Message.USER_NOT_FOUND);
        }
        return users;
    }

    @Override
    public ResponseEntity<Object> forgotPassword(String email) {
        return null;
    }

    @Override
    public void resetPassword(ChangePasswordDto request, String token) {
        // Validate the token
        if (!jwtUtils.validateJwtToken(token)) {
            throw new BadRequestException(Message.INVALID_TOKEN);
        }

        // Get the username from the token
        String username = jwtUtils.getUserNameFromJwtToken(token);

        // Find the user by username
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException(Message.USER_NOT_FOUND);
        }
        //check null field
        if (request.getNewPassword() == null || request.getConfirmPassword() == null || request.getOldPassword() == null) {
            throw new BadRequestException(Message.NULL_FILED);
        }

        //check Old password
        if (!encoder.matches(request.getOldPassword(), user.get().getPassword())) {
            throw new BadRequestException(Message.INVALID_OLD_PASSWORD);
        }

        // check new password match Regex
        if (!request.getNewPassword().matches(Constants.REGEX_PASSWORD)) {
            throw new BadRequestException(Message.INVALID_PASSWORD);
        }

        // Validate the new password and confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(Message.NOT_MATCH_PASSWORD);
        }

        // Update the user's password
        user.get().setPassword(encoder.encode(request.getNewPassword()));
        user.get().setLastModifiedDate(Instant.now());
        user.get().setLastModifiedBy(username);
        userRepository.save(user.get());
    }
    @Override
    public void uploadImage(final Long id ,final MultipartFile file) {
        // Check if the file is empty
        if (file.isEmpty()) {
            throw new BadRequestException(Message.EMPTY_FILE);
        }

        // Check if the file is an image
        if (!FileUploadUtil.isAllowedExtension(file.getOriginalFilename(), FileUploadUtil.IMAGE_PATTERN)) {
            throw new BadRequestException(Message.INVALID_FILE);
        }

        // Check if the file size is greater than 2MB
        if (file.getSize() > FileUploadUtil.MAX_FILE_SIZE) {
            throw new BadRequestException(Message.INVALID_FILE_SIZE);
        }
        User user = userRepository.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException(Message.USER_NOT_FOUND);
        }
        String customFileName = user.getFullName() + "avatar";

        // Upload the image to Cloudinary
        CloudinaryResponse cloudinaryResponse = cloudinaryService.upLoadFile(file, FileUploadUtil.getFileName(customFileName));

        // Save the image URL to the database
        user.setAvatar(cloudinaryResponse.getUrl());
        user.setLastModifiedBy(user.getFullName());
        user.setLastModifiedDate(Instant.now());
        userRepository.save(user);
    }

    public boolean checkUserAge(UserDto userDto) {
        LocalDate birthDate = userDto.getDob().atZone(ZoneId.systemDefault()).toLocalDate();
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return age >= 18;
    }
}
