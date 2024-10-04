package com.fu.pha.Service;

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
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.RoleRepository;
import com.fu.pha.repository.UserRepository;
import com.fu.pha.security.impl.UserDetailsImpl;
import com.fu.pha.security.jwt.JwtUtils;
import com.fu.pha.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @InjectMocks
    private UserServiceImpl userService;

    private LoginDtoRequest loginDtoRequest;
    private User user;
    private UserDetailsImpl userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        loginDtoRequest = new LoginDtoRequest();
        loginDtoRequest.setUsername("testuser");
        loginDtoRequest.setPassword("password");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");

        userDetails = new UserDetailsImpl(user, null);

        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

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

    @Test
    void testLoginWithNullUsername() {
        loginDtoRequest.setUsername(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.login(loginDtoRequest);
        });

        assertEquals(Message.MUST_FILL_USERNAME, exception.getMessage());
    }

    @Test
    void testLoginWithEmptyUsername() {
        loginDtoRequest.setUsername("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.login(loginDtoRequest);
        });

        assertEquals(Message.MUST_FILL_USERNAME, exception.getMessage());
    }

    @Test
    void testLoginWithNullPassword() {
        loginDtoRequest.setPassword(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.login(loginDtoRequest);
        });

        assertEquals(Message.MUST_FILL_PASSWORD, exception.getMessage());
    }

    @Test
    void testLoginWithEmptyPassword() {
        loginDtoRequest.setPassword("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.login(loginDtoRequest);
        });

        assertEquals(Message.MUST_FILL_PASSWORD, exception.getMessage());
    }

    @Test
    void testLoginWithInvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException(Message.INVALID_USERNAME));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            userService.login(loginDtoRequest);
        });

        assertEquals(Message.INVALID_USERNAME, exception.getMessage());
    }

    @Test
    void testCreateUserWithExistingUsername() {
        UserDto userDto = new UserDto();
        userDto.setUsername("existinguser");
        userDto.setEmail("newuser@gmail.com");
        userDto.setCic("123456789103");
        userDto.setPhone("1234567890");
        Set<RoleDto> roles = new HashSet<>();
        roles.add(new RoleDto(1, "ROLE_STOCK"));
        roles.add(new RoleDto(2, "ROLE_SALE"));

        userDto.setRolesDto(roles);
        userDto.setDob(Instant.parse("2002-01-01T00:00:00Z"));
        userDto.setFullName("New User");
        userDto.setAddress("123 Street");
        userDto.setGender(Gender.MALE);
        userDto.setStatus(Status.ACTIVE);
        userDto.setNote("Note");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(new User()));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.EXIST_USERNAME, exception.getMessage());
    }

    @Test
    void testCreateUserWithExistingEmail() {
        UserDto userDto = new UserDto();
        userDto.setUsername("existinguser");
        userDto.setEmail("newuser@gmail.com");
        userDto.setCic("123456789103");
        userDto.setPhone("1234567890");
        Set<RoleDto> roles = new HashSet<>();
        roles.add(new RoleDto(1, "ROLE_STOCK"));
        roles.add(new RoleDto(2, "ROLE_SALE"));

        userDto.setRolesDto(roles);
        userDto.setDob(Instant.parse("2002-01-01T00:00:00Z"));
        userDto.setFullName("New User");
        userDto.setAddress("123 Street");
        userDto.setGender(Gender.MALE);
        userDto.setStatus(Status.ACTIVE);
        userDto.setNote("Note");

        when(userRepository.getUserByEmail(anyString())).thenReturn(Optional.of(new User()));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.EXIST_EMAIL, exception.getMessage());
    }

    @Test
    void testCreateUserWithExistingCic() {
        UserDto userDto = new UserDto();
        userDto.setUsername("existinguser");
        userDto.setEmail("newuser@gmail.com");
        userDto.setCic("123456789103");
        userDto.setPhone("1234567890");
        Set<RoleDto> roles = new HashSet<>();
        roles.add(new RoleDto(1, "ROLE_STOCK"));
        roles.add(new RoleDto(2, "ROLE_SALE"));

        userDto.setRolesDto(roles);
        userDto.setDob(Instant.parse("2002-01-01T00:00:00Z"));
        userDto.setFullName("New User");
        userDto.setAddress("123 Street");
        userDto.setGender(Gender.MALE);
        userDto.setStatus(Status.ACTIVE);
        userDto.setNote("Note");

        when(userRepository.getUserByCic(anyString())).thenReturn(Optional.of(new User()));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.EXIST_CCCD, exception.getMessage());
    }

    @Test
    void testCreateUserWithExistingPhone() {
        UserDto userDto = new UserDto();
        userDto.setUsername("existinguser");
        userDto.setEmail("newuser@gmail.com");
        userDto.setCic("123456789103");
        userDto.setPhone("1234567890");
        Set<RoleDto> roles = new HashSet<>();
        roles.add(new RoleDto(1, "ROLE_STOCK"));
        roles.add(new RoleDto(2, "ROLE_SALE"));

        userDto.setRolesDto(roles);
        userDto.setDob(Instant.parse("2002-01-01T00:00:00Z"));
        userDto.setFullName("New User");
        userDto.setAddress("123 Street");
        userDto.setGender(Gender.MALE);
        userDto.setStatus(Status.ACTIVE);
        userDto.setNote("Note");

        when(userRepository.getUserByPhone(anyString())).thenReturn(Optional.of(new User()));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.EXIST_PHONE, exception.getMessage());
    }


    @Test
    void testCreateUserWithNullUsername() {
        UserDto userDto = new UserDto();
        userDto.setUsername(null);
        userDto.setEmail("newuser@gmail.com");
        userDto.setCic("123456789103");
        userDto.setPhone("1234567890");
        Set<RoleDto> roles = new HashSet<>();
        roles.add(new RoleDto(1, "ROLE_STOCK"));
        roles.add(new RoleDto(2, "ROLE_SALE"));

        userDto.setRolesDto(roles);
        userDto.setDob(Instant.parse("2002-01-01T00:00:00Z"));
        userDto.setFullName("New User");
        userDto.setAddress("123 Street");
        userDto.setGender(Gender.MALE);
        userDto.setStatus(Status.ACTIVE);
        userDto.setNote("Note");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    @Test
    void testCreateUserWithNullEmail() {
        UserDto userDto = new UserDto();
        userDto.setEmail(null);
        userDto.setUsername("existinguser");
        userDto.setCic("123456789103");
        userDto.setPhone("1234567890");
        Set<RoleDto> roles = new HashSet<>();
        roles.add(new RoleDto(1, "ROLE_STOCK"));
        roles.add(new RoleDto(2, "ROLE_SALE"));

        userDto.setRolesDto(roles);
        userDto.setDob(Instant.parse("2002-01-01T00:00:00Z"));
        userDto.setFullName("New User");
        userDto.setAddress("123 Street");
        userDto.setGender(Gender.MALE);
        userDto.setStatus(Status.ACTIVE);
        userDto.setNote("Note");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    @Test
    void testCreateUserWithNullPhone() {
        UserDto userDto = new UserDto();
        userDto.setPhone(null);
        userDto.setUsername("existinguser");
        userDto.setEmail("newuser@gmail.com");
        userDto.setCic("123456789103");
        Set<RoleDto> roles = new HashSet<>();
        roles.add(new RoleDto(1, "ROLE_STOCK"));
        roles.add(new RoleDto(2, "ROLE_SALE"));

        userDto.setRolesDto(roles);
        userDto.setDob(Instant.parse("2002-01-01T00:00:00Z"));
        userDto.setFullName("New User");
        userDto.setAddress("123 Street");
        userDto.setGender(Gender.MALE);
        userDto.setStatus(Status.ACTIVE);
        userDto.setNote("Note");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    @Test
    void testCreateUserWithNullCic() {
        UserDto userDto = new UserDto();
        userDto.setCic(null);
        userDto.setUsername("existinguser");
        userDto.setEmail("newuser@gmail.com");
        userDto.setPhone("1234567890");
        Set<RoleDto> roles = new HashSet<>();
        roles.add(new RoleDto(1, "ROLE_STOCK"));
        roles.add(new RoleDto(2, "ROLE_SALE"));

        userDto.setRolesDto(roles);
        userDto.setDob(Instant.parse("2002-01-01T00:00:00Z"));
        userDto.setFullName("New User");
        userDto.setAddress("123 Street");
        userDto.setGender(Gender.MALE);
        userDto.setStatus(Status.ACTIVE);
        userDto.setNote("Note");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    @Test
    void testUpdateUser_UserNotFound() {
        UserDto userDto = new UserDto();
        userDto.setCic("123456789876");
        userDto.setUsername("existinguser");
        userDto.setEmail("newuser@gmail.com");
        userDto.setPhone("1234567890");
        Set<RoleDto> roles = new HashSet<>();
        roles.add(new RoleDto(1, "ROLE_STOCK"));
        roles.add(new RoleDto(2, "ROLE_SALE"));

        userDto.setRolesDto(roles);
        userDto.setDob(Instant.parse("2002-01-01T00:00:00Z"));
        userDto.setFullName("New User");
        userDto.setAddress("123 Street");
        userDto.setGender(Gender.MALE);
        userDto.setStatus(Status.ACTIVE);
        userDto.setNote("Note");

        userDto.setId(1L);

        when(userRepository.getUserById(userDto.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }


}
