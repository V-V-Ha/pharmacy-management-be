package com.fu.pha.Service;

import com.fu.pha.dto.request.ChangePasswordDto;
import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.dto.request.RoleDto;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.JwtResponse;
import com.fu.pha.entity.Role;
import com.fu.pha.entity.User;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.Gender;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.exception.Message;
import com.fu.pha.repository.RoleRepository;
import com.fu.pha.repository.UserRepository;
import com.fu.pha.security.impl.UserDetailsImpl;
import com.fu.pha.security.jwt.JwtUtils;
import com.fu.pha.service.EmailService;
import com.fu.pha.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;

    private LoginDtoRequest loginDtoRequest;
    private UserDetailsImpl userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Khởi tạo dữ liệu mẫu cho các test case
        loginDtoRequest = new LoginDtoRequest();
        loginDtoRequest.setUsername("minhhieu");
        loginDtoRequest.setPassword("Minhhieu2311");

        user = new User();
        user.setId(1L);
        user.setUsername("minhhieu");
        user.setEmail("lekhacminhhieu2311el@gmail.com");

        userDetails = new UserDetailsImpl(user, null);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    //Test trường hợp đăng nhập thành công
    @Test
    void testLoginSuccess() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwtToken");

        JwtResponse response = userService.login(loginDtoRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getEmail());
    }

    // Test trường hợp username sai
    @Test
    void testLogin_UsernameFalse() {
        loginDtoRequest.setUsername("minhhieu1");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException(Message.INVALID_USERNAME));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            userService.login(loginDtoRequest);
        });

        assertEquals(Message.INVALID_USERNAME, exception.getMessage());
    }

    // Test trường hợp password sai
    @Test
    void testLogin_PasswordFalse() {
        loginDtoRequest.setPassword("Minhhieu23112002");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException(Message.INVALID_USERNAME));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            userService.login(loginDtoRequest);
        });

        assertEquals(Message.INVALID_USERNAME, exception.getMessage());
    }

    // Test trường hợp username là null
    @Test
    void testLogin_NullUsername() {
        loginDtoRequest.setUsername(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.login(loginDtoRequest);
        });

        assertEquals(Message.MUST_FILL_USERNAME, exception.getMessage());
    }

    // Test trường hợp password là null
    @Test
    void testLogin_NullPassword() {
        loginDtoRequest.setPassword(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.login(loginDtoRequest);
        });

        assertEquals(Message.MUST_FILL_PASSWORD, exception.getMessage());
    }

    // Test trường hợp quên mật khẩu thành công
    @Test
    void testForgotPassword_Success() {
        String email = "lekhacminhhieu2311el@gmail.com";
        User user = new User();
        user.setEmail(email);
        user.setFullName("LE KHAC MINH HIEU");

        when(userRepository.getUserByEmail(email)).thenReturn(Optional.of(user));
       // when(encoder.encode(anyString())).thenReturn("encodedPassword");

        userService.forgotPassword(email);

        verify(userRepository).save(user);
        verify(emailService).sendSimpleEmail(eq(email), anyString(), contains("Mật khẩu của bạn là:"));
    }

    // Test trường hợp quên mật khẩu với email là null
    @Test
    void testForgotPassword_NullEmail() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.forgotPassword(null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp quên mật khẩu với email không tồn tại
    @Test
    void testForgotPassword_NonExistentEmail() {
        String email = "lekhachieu@gmail.com";

        when(userRepository.getUserByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.forgotPassword(email);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }

    // Test trường hợp reset mật khẩu thành công
    @Test
    void testResetPassword_Success() {
        String token = "validToken";
        ChangePasswordDto request = new ChangePasswordDto("Minhhieu2311", "Minhhieu23112002", "Minhhieu23112002");

        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn("minhhieu");
        when(userRepository.findByUsername("minhhieu")).thenReturn(Optional.of(user));
        when(encoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(true);
        when(encoder.encode(request.getNewPassword())).thenReturn("Minhhieu23112002");

        userService.resetPassword(request, token);

        verify(userRepository).save(user);
        assertEquals("Minhhieu23112002", user.getPassword());
    }

    // Test trường hợp reset mật khẩu với mật khẩu cũ không chính xác
    @Test
    void testResetPassword_OldPasswordNotCorrect() {
        String token = "validToken";
        ChangePasswordDto request = new ChangePasswordDto("minhhieu", "Minhhieu23112002", "Minhhieu23112002");

        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn("minhhieu");
        when(userRepository.findByUsername("minhhieu")).thenReturn(Optional.of(user));
        when(encoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.resetPassword(request, token);
        });

        assertEquals(Message.INVALID_OLD_PASSWORD, exception.getMessage());
    }

    // Test trường hợp reset mật khẩu với mật khẩu cũ là null
    @Test
    void testResetPassword_NullOldPassword() {
        String token = "validToken";
        ChangePasswordDto request = new ChangePasswordDto(null, "Minhhieu23112002", "Minhhieu23112002");

        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn("minhhieu");
        when(userRepository.findByUsername("minhhieu")).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.resetPassword(request, token);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp reset mật khẩu với mật khẩu mới không hợp lệ
    @Test
    void testResetPassword_InvalidNewPassword() {
        String token = "validToken";
        ChangePasswordDto request = new ChangePasswordDto("Minhhieu2311", "minhhieu", "Minhhieu23112002");

        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn("minhhieu");
        when(userRepository.findByUsername("minhhieu")).thenReturn(Optional.of(user));
        when(encoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.resetPassword(request, token);
        });

        assertEquals(Message.INVALID_PASSWORD, exception.getMessage());
    }

    // Test trường hợp reset mật khẩu với mật khẩu mới là null
    @Test
    void testResetPassword_NullNewPassword() {
        String token = "validToken";
        ChangePasswordDto request = new ChangePasswordDto("Minhhieu2311", null, "Minhhieu23112002");

        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn("minhhieu");
        when(userRepository.findByUsername("minhhieu")).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.resetPassword(request, token);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp reset mật khẩu với mật khẩu xác nhận không khớp
    @Test
    void testResetPassword_ConfirmPasswordNotMatch() {
        String token = "validToken";
        ChangePasswordDto request = new ChangePasswordDto("Minhhieu2311", "Minhhieu23112002", "123");

        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn("minhhieu");
        when(userRepository.findByUsername("minhhieu")).thenReturn(Optional.of(user));
        when(encoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.resetPassword(request, token);
        });

        assertEquals(Message.NOT_MATCH_PASSWORD, exception.getMessage());
    }

    // Test trường hợp reset mật khẩu với mật khẩu xác nhận là null
    @Test
    void testResetPassword_NullConfirmPassword() {
        String token = "validToken";
        ChangePasswordDto request = new ChangePasswordDto("Minhhieu2311", "Minhhieu23112002", null);

        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn("minhhieu");
        when(userRepository.findByUsername("minhhieu")).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.resetPassword(request, token);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    @BeforeEach
    void setUpCreate() {
        userDto = new UserDto();
        userDto.setUsername("havv123");
        userDto.setEmail("vuha13052002@gmail.com");
        userDto.setFullName("Vũ Văn Hà");
        userDto.setPhone("0987654321");
        userDto.setCic("012345678912");
        userDto.setDob(Instant.parse("2002-10-10T00:00:00Z"));
        userDto.setAddress("Ngõ 45 Đường Láng");
        userDto.setGender(Gender.MALE);

        // Add at least one valid role to rolesDto
        Set<RoleDto> rolesDto = new HashSet<>();
        RoleDto roleDto = new RoleDto();
        roleDto.setName("ROLE_PRODUCT_OWNER"); // Use an appropriate role name
        rolesDto.add(roleDto);
        userDto.setRolesDto(rolesDto);

        userDto.setStatus(Status.ACTIVE);

        user = new User();
        user.setUsername("havv123");
        user.setEmail("vuha13052002@gmail.com");
        user.setFullName("Vũ Văn Hà");
        user.setPhone("0987654321");
        user.setCic("012345678912");
        user.setDob(Instant.parse("2002-10-10T00:00:00Z"));
        user.setAddress("Ngõ 45 Đường Láng");
        user.setGender(Gender.MALE);
        user.setRoles(new HashSet<>());
        user.setStatus(Status.ACTIVE);
    }

    // Test trường hợp tạo user thành công
    @Test
    void testCreateUser_Success() {
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.getUserByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.getUserByCic(userDto.getCic())).thenReturn(Optional.empty());
        when(userRepository.getUserByPhone(userDto.getPhone())).thenReturn(Optional.empty());
        when(encoder.encode(anyString())).thenReturn("encodedPassword");

        // Mock role lookup
        Role role = new Role();
        role.setName(ERole.ROLE_PRODUCT_OWNER); // Use the same role as in setUpCreate
        when(roleRepository.findByName(ERole.ROLE_PRODUCT_OWNER)).thenReturn(Optional.of(role));

        userService.createUser(userDto, null);

        verify(userRepository).save(any(User.class));
    }

    // Test trường hợp tạo user với full name không hợp lệ
    @Test
    void testCreateUser_InvalidFullName() {
        userDto.setFullName("Vũ Văn H123");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.INVALID_NAME, exception.getMessage());
    }

    // Test trường hợp tạo user với full name là null
    @Test
    void testCreateUser_NullFullName() {
        userDto.setFullName("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp tạo user với email không hợp lệ
    @Test
    void testCreateUser_InvalidEmail() {
        userDto.setEmail("vuha2002@gmail.co");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.INVALID_GMAIL, exception.getMessage());
    }

    // Test trường hợp tạo user với email là null
    @Test
    void testCreateUser_NullEmail() {
        userDto.setEmail("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp tạo user với phone không hợp lệ
    @Test
    void testCreateUser_InvalidPhone() {
        userDto.setPhone("'098765432a");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.INVALID_PHONE, exception.getMessage());
    }

    // Test trường hợp tạo user với phone là null
    @Test
    void testCreateUser_NullPhone() {
        userDto.setPhone("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp tạo user với không đủ 18 tuổi
    @Test
    void testCreateUser_UserUnderAge() {
       userDto.setDob(Instant.now().minus(Duration.ofDays(365 * 17)));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.INVALID_AGE, exception.getMessage());
    }

    // Test trường hợp tạo user với dob là null
    @Test
    void testCreateUser_NullDob() {
        userDto.setDob(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    @Test
    void testCreateUser_InvalidAddress() {
        userDto.setAddress("Ngõ 5, Đường Láng");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.INVALID_ADDRESS, exception.getMessage());
    }

    // Test trường hợp tạo user với address là null
    @Test
    void testCreateUser_NullAddress() {
        userDto.setAddress("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp tạo user với gender là null
    @Test
    void testCreateUser_NullGender() {
        userDto.setGender(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp tạo user với cic không hợp lệ
    @Test
    void testCreateUser_InvalidCic() {
        userDto.setCic("1234567890123");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.INVALID_CCCD, exception.getMessage());
    }

    // Test trường hợp tạo user với cic là null
    @Test
    void testCreateUser_NullCic() {
        userDto.setCic("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp tạo user với username không hợp lệ
    @Test
    void testCreateUser_InvalidUsername() {
        userDto.setUsername("havv");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.INVALID_USERNAME_C, exception.getMessage());
    }

    // Test trường hợp tạo user với username là null
    @Test
    void testCreateUser_NullUsername() {
        userDto.setUsername("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp tạo user với role là null
    @Test
    void testCreateUser_NullRole() {
        userDto.setRolesDto(new HashSet<>());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    @BeforeEach
    void setUpUpdate() {
        userDto = new UserDto();
        userDto.setId(3L);
        userDto.setUsername("havv123");
        userDto.setEmail("vuha13052002@gmail.com");
        userDto.setFullName("Vũ Văn Hà");
        userDto.setPhone("0987654321");
        userDto.setCic("012345678912");
        userDto.setDob(Instant.parse("2002-10-10T00:00:00Z"));
        userDto.setAddress("Ngõ 45 Đường Láng");
        userDto.setGender(Gender.MALE);
        userDto.setStatus(Status.ACTIVE);
        Set<RoleDto> rolesDto = new HashSet<>();
        RoleDto roleDto = new RoleDto();
        roleDto.setName(ERole.ROLE_PRODUCT_OWNER.toString());
        rolesDto.add(roleDto);
        userDto.setRolesDto(rolesDto);

        user = new User();
        user.setId(3L);
        user.setUsername("havv123");
        user.setEmail("vuha13052002@gmail.com");
        user.setFullName("Vũ Văn Hà");
        user.setPhone("0987654321");
        user.setCic("012345678912");
        user.setDob(Instant.parse("2002-10-10T00:00:00Z"));
        user.setAddress("Ngõ 45 Đường Láng");
        user.setGender(Gender.MALE);
        user.setRoles(new HashSet<>());
        user.setStatus(Status.ACTIVE);
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(ERole.ROLE_PRODUCT_OWNER);
        roles.add(role);
        user.setRoles(roles);
    }

    // Test trường hợp cập nhật user thành công
    @Test
    void testUpdateUser_Success() {
        when(userRepository.getUserById(userDto.getId())).thenReturn(Optional.of(user));
        when(userRepository.getUserByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.getUserByPhone(userDto.getPhone())).thenReturn(Optional.empty());
        when(userRepository.getUserByCic(userDto.getCic())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.empty());

        Role role = new Role();
        role.setName(ERole.ROLE_PRODUCT_OWNER); // Use the same role as in setUpUpdate
        when(roleRepository.findByName(ERole.ROLE_PRODUCT_OWNER)).thenReturn(Optional.of(role));

        userService.updateUser(userDto, null);

        verify(userRepository).save(any(User.class));
    }

    // Test trường hợp cập nhật user với không tìm thâ user
    @Test
    void testUpdateUser_NotFoundUser() {
        // Arrange
        userDto.setId(123L); // Set the user ID to 123
        when(userRepository.getUserById(123L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }

    // Test trường hợp cập nhật user với full name không hợp lệ
    @Test
    void testUpdateUser_InvalidFullName() {
        userDto.setFullName("Vũ Văn H123");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.INVALID_NAME, exception.getMessage());
    }

    // Test trường hợp cập nhật user với full name là null
    @Test
    void testUpdateUser_NullFullName() {
        userDto.setFullName("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với email không hợp lệ
    @Test
    void testUpdateUser_InvalidEmail() {
        userDto.setEmail("vuha2002@gmail.co");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.INVALID_GMAIL, exception.getMessage());
    }

    // Test trường hợp cập nhật user với email là null
    @Test
    void testUpdateUser_NullEmail() {
        userDto.setEmail("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với phone không hợp lệ
    @Test
    void testUpdateUser_InvalidPhone() {
        userDto.setPhone("098765432a");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.INVALID_PHONE, exception.getMessage());
    }

    // Test trường hợp cập nhật user với phone là null
    @Test
    void testUpdateUser_NullPhone() {
        userDto.setPhone("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với không đủ 18 tuổi
    @Test
    void testUpdateUser_UserUnderAge() {
        userDto.setDob(Instant.now().minus(Duration.ofDays(365 * 17)));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.INVALID_AGE, exception.getMessage());
    }

    // Test trường hợp cập nhật user với dob là null
    @Test
    void testUpdateUser_NullDob() {
        userDto.setDob(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với address không hợp lệ
    @Test
    void testUpdateUser_InvalidAddress() {
        userDto.setAddress("Ngõ 5, Đường Láng");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.INVALID_ADDRESS, exception.getMessage());
    }

    // Test trường hợp cập nhật user với address là null
    @Test
    void testUpdateUser_NullAddress() {
        userDto.setAddress("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với gender là null
    @Test
    void testUpdateUser_NullGender() {
        userDto.setGender(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với cic không hợp lệ
    @Test
    void testUpdateUser_InvalidCic() {
        userDto.setCic("1234567890123");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.INVALID_CCCD, exception.getMessage());
    }

    // Test trường hợp cập nhật user với cic là null
    @Test
    void testUpdateUser_NullCic() {
        userDto.setCic("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với username không hợp lệ
    @Test
    void testUpdateUser_InvalidUsername() {
        userDto.setUsername("havv");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.INVALID_USERNAME_C, exception.getMessage());
    }

    // Test trường hợp cập nhật user với username là null
    @Test
    void testUpdateUser_NullUsername() {
        userDto.setUsername("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với role là null
    @Test
    void testUpdateUser_NullRole() {
        userDto.setRolesDto(new HashSet<>());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp view user detail thành công
    @Test
    void testViewUserDetail_Success() {
        Long userId = 3L;
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserDto userDto = userService.viewDetailUser(userId);
        assertNotNull(userDto);
        assertEquals(userId, userDto.getId());
    }

    // Test trường hợp view user detail với không tìm thấy user
    @Test
    void testViewUserDetail_NotFoundUser() {
        Long userId = 200L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.viewDetailUser(userId);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }

    // Test trường hợp active user thành công
    @Test
    void testActiveUser_Success() {
        Long userId = 3L;
        User user = new User();
        user.setId(userId);
        user.setStatus(Status.INACTIVE);

        when(userRepository.getUserById(userId)).thenReturn(Optional.of(user));

        UserDto userDto = new UserDto();
        userDto.setId(userId);

        userService.activeUser(userDto);

        verify(userRepository).save(user);
        assertEquals(Status.ACTIVE, user.getStatus());
        assertEquals(Message.UPDATE_SUCCESS, "Cập nhật thành công");
    }

    // Test trường hợp active user với không tìm thấy user
    @Test
    void testActiveUser_NotFoundUser() {
        Long userId = 200L;

        when(userRepository.getUserById(userId)).thenReturn(Optional.empty());

        UserDto userDto = new UserDto();
        userDto.setId(userId);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.activeUser(userDto);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }

    // Test trường hợp deActive user thành công
    @Test
    void testDeActiveUser_Success() {
        Long userId = 3L;
        User user = new User();
        user.setId(userId);
        user.setStatus(Status.ACTIVE);

        when(userRepository.getUserById(userId)).thenReturn(Optional.of(user));

        UserDto userDto = new UserDto();
        userDto.setId(userId);

        userService.deActiveUser(userDto);

        verify(userRepository).save(user);
        assertEquals(Status.INACTIVE, user.getStatus());
        assertEquals(Message.UPDATE_SUCCESS, "Cập nhật thành công");
    }

    // Test trường hợp deActive user với không tìm thấy user
    @Test
    void testDeActiveUser_NotFoundUser() {
        Long userId = 200L;

        when(userRepository.getUserById(userId)).thenReturn(Optional.empty());

        UserDto userDto = new UserDto();
        userDto.setId(userId);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.deActiveUser(userDto);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }
}
