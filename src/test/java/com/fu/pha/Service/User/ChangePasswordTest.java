package com.fu.pha.Service.User;

import com.fu.pha.dto.request.ChangePasswordDto;
import com.fu.pha.entity.User;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.repository.UserRepository;
import com.fu.pha.security.jwt.JwtUtils;
import com.fu.pha.service.EmailService;
import com.fu.pha.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChangePasswordTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;
    private User user;


    // Test trường hợp reset mật khẩu thành công
    @Test
    void UTCCP01() {
        String token = "validToken";
        ChangePasswordDto request = new ChangePasswordDto("Minhhieu2311", "Minhhieu23112002", "Minhhieu23112002");

        User user = new User();
        user.setUsername("minhhieu");
        user.setPassword("Minhhieu2311");

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
    void UTCCP02() {
        String token = "validToken";
        ChangePasswordDto request = new ChangePasswordDto("minhhieu", "Minhhieu23112002", "Minhhieu23112002");

        User user = new User();
        user.setUsername("minhhieu");
        user.setPassword("Minhhieu2311");

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
    void UTCCP03() {
        String token = "validToken";
        ChangePasswordDto request = new ChangePasswordDto(null, "Minhhieu23112002", "Minhhieu23112002");

        User user = new User();
        user.setUsername("minhhieu");
        user.setPassword("Minhhieu2311");

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
    void UTCCP04() {
        String token = "validToken";
        ChangePasswordDto request = new ChangePasswordDto("Minhhieu2311", "minhhieu", "Minhhieu23112002");

        User user = new User();
        user.setUsername("minhhieu");
        user.setPassword("Minhhieu2311");

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
    void UTCCP05() {
        String token = "validToken";
        ChangePasswordDto request = new ChangePasswordDto("Minhhieu2311", null, "Minhhieu23112002");

        User user = new User();
        user.setUsername("minhhieu");
        user.setPassword("Minhhieu2311");

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
    void UTCCP06() {
        String token = "validToken";
        ChangePasswordDto request = new ChangePasswordDto("Minhhieu2311", "Minhhieu23112002", "123");

        User user = new User();
        user.setUsername("minhhieu");
        user.setPassword("Minhhieu2311");

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
    void UTCCP07() {
        String token = "validToken";
        ChangePasswordDto request = new ChangePasswordDto("Minhhieu2311", "Minhhieu23112002", null);

        User user = new User();
        user.setUsername("minhhieu");
        user.setPassword("Minhhieu2311");

        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn("minhhieu");
        when(userRepository.findByUsername("minhhieu")).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.resetPassword(request, token);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }
}
