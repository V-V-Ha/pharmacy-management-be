package com.fu.pha.Service;

import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.dto.response.JwtResponse;
import com.fu.pha.entity.User;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserServiceImpl userService;

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
    void UTCLG01() {
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
    void UTCLG02() {
        loginDtoRequest.setUsername("minhhieu1");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException(Message.INVALID_USERNAME));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            userService.login(loginDtoRequest);
        });

        assertEquals(Message.INVALID_USERNAME, exception.getMessage());
    }

    // Test trường hợp username là null
    @Test
    void UTCLG03() {
        loginDtoRequest.setUsername(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.login(loginDtoRequest);
        });

        assertEquals(Message.MUST_FILL_USERNAME, exception.getMessage());
    }

    // Test trường hợp password sai
    @Test
    void UTCLG04() {
        loginDtoRequest.setPassword("Minhhieu23112002");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException(Message.INVALID_USERNAME));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            userService.login(loginDtoRequest);
        });

        assertEquals(Message.INVALID_USERNAME, exception.getMessage());
    }

    // Test trường hợp password là null
    @Test
    void UTCLG05() {
        loginDtoRequest.setPassword(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.login(loginDtoRequest);
        });

        assertEquals(Message.MUST_FILL_PASSWORD, exception.getMessage());
    }
}
