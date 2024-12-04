package com.fu.pha.service.impl;

import com.fu.pha.dto.request.ChangePasswordDto;
import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.dto.request.RoleDto;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.CloudinaryResponse;

import com.fu.pha.dto.response.JwtResponse;
import com.fu.pha.entity.Role;
import com.fu.pha.entity.User;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.MaxUploadSizeExceededException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.RoleRepository;
import com.fu.pha.repository.UserRepository;
import com.fu.pha.security.impl.UserDetailsImpl;
import com.fu.pha.security.jwt.JwtUtils;
import com.fu.pha.service.CloudinaryService;
import com.fu.pha.service.EmailService;
import com.fu.pha.util.FileUploadUtil;
import com.fu.pha.validate.Constants;
import com.fu.pha.validate.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    EmailService emailService;

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

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        userRepository.findByUsername(loginDtoRequest.getUsername())
                .ifPresent(user -> {
                    user.setFcmToken(loginDtoRequest.getFcmToken());
                    userRepository.save(user);
                });

        return new JwtResponse(
                jwt,
                loginDtoRequest.getFcmToken(),
                userDetails.getUser().getId(),
                userDetails.getUsername(),
                userDetails.getUser().getEmail(),
                roles, userDetails.getUser().getAvatar(),
                userDetails.getUser().getFullName());
    }

    @Transactional
    @Override
    public void createUser(UserDto userDto, MultipartFile file) {

        // Kiểm tra các giá trị đầu vào từ DTO
        checkValidate(userDto);

        // Kiểm tra sự tồn tại của username, email, cic và phone
        userRepository.findByUsername(userDto.getUsername())
                .ifPresent(user -> { throw new BadRequestException(Message.EXIST_USERNAME); });

        userRepository.getUserByEmail(userDto.getEmail())
                .ifPresent(user -> { throw new BadRequestException(Message.EXIST_EMAIL); });

        userRepository.getUserByCic(userDto.getCic())
                .ifPresent(user -> { throw new BadRequestException(Message.EXIST_CCCD); });

        userRepository.getUserByPhone(userDto.getPhone())
                .ifPresent(user -> { throw new BadRequestException(Message.EXIST_PHONE); });

        // Xử lý các vai trò người dùng
        validateRoles(userDto.getRolesDto());

        // Tạo đối tượng User từ UserDto
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setAddress(userDto.getAddress());
        user.setDob(userDto.getDob());
        user.setGender(userDto.getGender());
        user.setFullName(userDto.getFullName());
        user.setPhone(userDto.getPhone());
        user.setCic(userDto.getCic());
        user.setStatus(userDto.getStatus());
        user.setNote(userDto.getNote());

        // Xử lý vai trò người dùng
        Set<Role> roles = userDto.getRolesDto().stream()
                .map(roleDto -> roleRepository.findByName(ERole.valueOf(roleDto.getName()))
                        .orElseThrow(() -> new ResourceNotFoundException(Message.ROLE_NOT_FOUND)))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        if (file != null && !file.isEmpty()) {
            String avatar = uploadImage(file);
            user.setAvatar(avatar);
        }

        // Tạo mật khẩu tạm thời
        String tempPassword = UUID.randomUUID().toString().substring(0, 7) + "A";
        user.setPassword(encoder.encode(tempPassword));

        // Gửi email thông báo tạo tài khoản
        String emailSubject = "[PHA] Tạo Tài khoản";
        String emailContent = "Xin chào, " + user.getFullName() + "\n\n" +
                "Chúng tôi chào mừng bạn đến với hệ thống Pharmacy Management System.\n" +
                "Đây là thông báo cấp tài khoản đến bạn.\n\n" +
                "Mật khẩu của bạn là: " + tempPassword + "\n\n" +
                "Trân trọng,\n\n" +
                "Pharmacy Management System";

        emailService.sendSimpleEmail(user.getEmail(), emailSubject, emailContent);
        userRepository.save(user);
    }

    private void validateRoles(Set<RoleDto> rolesDto) {
        boolean hasProductOwner = rolesDto.stream().anyMatch(role -> role.getName().equals(ERole.ROLE_PRODUCT_OWNER.name()));
        boolean hasSaleOrStock = rolesDto.stream().anyMatch(role -> role.getName().equals(ERole.ROLE_SALE.name()) || role.getName().equals(ERole.ROLE_STOCK.name()));

        if (hasProductOwner && hasSaleOrStock) {
            throw new BadRequestException(Message.INVALID_ROLE_COMBINATION);
        }
    }

    @Transactional
    @Override
    public void updateUser(UserDto userDto , MultipartFile file) {
        // Validate user information
        checkValidate(userDto);

        // Retrieve the user by ID
        User user = userRepository.getUserById(userDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        // Validate roles
        validateRoles(userDto.getRolesDto());

        // Check for existing users with the same email, phone, CIC, and username
        userRepository.getUserByEmail(userDto.getEmail())
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {
                    throw new BadRequestException(Message.EXIST_EMAIL);
                });

        userRepository.getUserByPhone(userDto.getPhone())
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {
                    throw new BadRequestException(Message.EXIST_PHONE);
                });

        userRepository.getUserByCic(userDto.getCic())
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {
                    throw new BadRequestException(Message.EXIST_CCCD);
                });

        userRepository.findByUsername(userDto.getUsername())
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {
                    throw new BadRequestException(Message.EXIST_USERNAME);
                });

        // Update user details
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setAddress(userDto.getAddress());
        user.setDob(userDto.getDob());
        user.setGender(userDto.getGender());
        user.setFullName(userDto.getFullName());
        user.setPhone(userDto.getPhone());
        user.setCic(userDto.getCic());
        user.setStatus(userDto.getStatus());
        user.setNote(userDto.getNote());

        // Update user roles
        Set<Role> roles = userDto.getRolesDto().stream()
                .map(roleDto -> roleRepository.findByName(ERole.valueOf(roleDto.getName()))
                        .orElseThrow(() -> new ResourceNotFoundException(Message.ROLE_NOT_FOUND)))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        // Upload the avatar if there is a file
        if (file != null && !file.isEmpty()) {
            String avatar = uploadImage(file);
            user.setAvatar(avatar);
        }

        // Save the updated user
        userRepository.save(user);
    }

    private void checkValidate(UserDto userDto) {
        if (userDto.getFullName().isEmpty() || userDto.getEmail().isEmpty() || userDto.getPhone().isEmpty() ||
                userDto.getDob() == null || userDto.getAddress().isEmpty() ||
                userDto.getGender() == null || userDto.getCic().isEmpty() || userDto.getUsername().isEmpty() ||
                userDto.getRolesDto().isEmpty() || userDto.getStatus() == null) {
            throw new ResourceNotFoundException(Message.NULL_FILED);
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
    }

    @Override
    public void activeUser(UserDto userDto) {
        // Retrieve the user by ID and throw exception if not found
        User user = userRepository.getUserById(userDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        // Activate the user
        user.setStatus(Status.ACTIVE);

        // Save the user
        userRepository.save(user);
    }

    @Override
    public void deActiveUser(UserDto userDto) {
        // Retrieve the user by ID and throw exception if not found
        User user = userRepository.getUserById(userDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        // Deactivate the user
        user.setStatus(Status.INACTIVE);

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
    public void forgotPassword(String email) {
        if (email.isEmpty()) {
            throw new BadRequestException(Message.NULL_FILED);
        }

        User user = userRepository.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        String tempPassword = UUID.randomUUID().toString().substring(0, 7) + "A";

        user.setPassword(encoder.encode(tempPassword));
        userRepository.save(user);

        String emailSubject = "[PHA] Yêu Cầu Đặt Lại Mật Khẩu";
        String emailContent = "Xin chào, " + user.getFullName() + "\n\n" +
                "Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn trên hệ thống Pharmacy Management System.\n" +
                "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.\n" +
                "Đây là thông báo phản hồi cho yêu cầu đặt lại mật khẩu tài khoản của bạn.\n\n" +
                "Mật khẩu của bạn là: " + tempPassword + "\n\n" +
                "Trân trọng,\n\n" +
                "Pharmacy Management System";

        emailService.sendSimpleEmail(user.getEmail(), emailSubject, emailContent);
    }

    @Override
    public void resetPassword(ChangePasswordDto request, String token) {
        // Validate the token
        if (!jwtUtils.validateJwtToken(token)) {
            throw new BadRequestException(Message.INVALID_TOKEN);
        }

        String username = jwtUtils.getUserNameFromJwtToken(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
            throw new BadRequestException(Message.NULL_FILED);
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new BadRequestException(Message.NULL_FILED);
        }
        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            throw new BadRequestException(Message.NULL_FILED);
        }


        if (!encoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException(Message.INVALID_OLD_PASSWORD);
        }

        if (!request.getNewPassword().matches(Constants.REGEX_PASSWORD)) {
            throw new BadRequestException(Message.INVALID_PASSWORD);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(Message.NOT_MATCH_PASSWORD);
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }


    public String uploadImage(final MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException(Message.EMPTY_FILE);
        }

        if (!FileUploadUtil.isAllowedExtension(file.getOriginalFilename(), FileUploadUtil.IMAGE_PATTERN)) {
            throw new BadRequestException(Message.INVALID_FILE);
        }

        if (file.getSize() > FileUploadUtil.MAX_FILE_SIZE) {
            throw new MaxUploadSizeExceededException(Message.INVALID_FILE_SIZE);
        }

        String customFileName = UUID.randomUUID() + "avatar";

        CloudinaryResponse cloudinaryResponse = cloudinaryService.upLoadFile(file, FileUploadUtil.getFileName(customFileName));

        return cloudinaryResponse.getUrl();
    }

    @Override
    public void updateUserStatus(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(Message.USER_NOT_FOUND));

        // Kiểm tra xem người dùng có vai trò PRODUCT_OWNER không
        boolean isProductOwner = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(ERole.ROLE_PRODUCT_OWNER));

        // Nếu là ROLE_PRODUCT_OWNER, không cho phép thay đổi trạng thái
        if (isProductOwner) {
            throw new BadRequestException("Không thể thay đổi trạng thái của chủ cửa hàng");
        }

        // Chuyển đổi trạng thái nếu không phải ROLE_PRODUCT_OWNER
        if (user.getStatus() == Status.ACTIVE) {
            user.setStatus(Status.INACTIVE);
        } else {
            user.setStatus(Status.ACTIVE);
        }

        userRepository.save(user);
    }

    public boolean checkUserAge(UserDto userDto) {
        LocalDate birthDate = userDto.getDob().atZone(ZoneId.systemDefault()).toLocalDate();
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return age >= 18;
    }
}