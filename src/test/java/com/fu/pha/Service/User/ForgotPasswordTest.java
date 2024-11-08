package com.fu.pha.Service.User;

import com.fu.pha.entity.User;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ForgotPasswordTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserServiceImpl userService;

    // Test trường hợp quên mật khẩu thành công
    @Test
    void UTCFP01() {
        String email = "lekhacminhhieu2311el@gmail.com";
        User user = new User();
        user.setEmail(email);
        user.setFullName("LE KHAC MINH HIEU");

        // Giả lập hành vi của userRepository, trả về đối tượng User khi gọi getUserByEmail với email
        when(userRepository.getUserByEmail(email)).thenReturn(Optional.of(user));

        // Giả lập hành vi của encoder, trả về chuỗi "encodedPassword" khi gọi phương thức encode với bất kỳ chuỗi nào
        when(encoder.encode(anyString())).thenReturn("encodedPassword");

        userService.forgotPassword(email);

        verify(userRepository).save(user);
        verify(emailService).sendSimpleEmail(eq(email), anyString(), contains("Mật khẩu của bạn là:"));
    }

    // Test trường hợp quên mật khẩu với email không tồn tại
    @Test
    void UTCFP02() {
        String email = "lekhachieu@gmail.com";

        when(userRepository.getUserByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.forgotPassword(email);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }

    // Test trường hợp quên mật khẩu với email là null
    @Test
    void UTCFP03() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.forgotPassword("");
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }
}
