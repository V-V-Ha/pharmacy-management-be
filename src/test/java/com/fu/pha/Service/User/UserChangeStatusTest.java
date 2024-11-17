package com.fu.pha.Service.User;


import com.fu.pha.dto.request.UserDto;
import com.fu.pha.entity.User;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.UserRepository;
import com.fu.pha.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserChangeStatusTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    // Test trường hợp change status user thành công
    @Test
    void UTCURCS01() {
        Long userId = 3L;
        User user = new User();
        user.setId(userId);
        user.setStatus(Status.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateUserStatus(userId);

        assertTrue(user.getStatus() == Status.INACTIVE);
        verify(userRepository).save(user);
    }

    // Test trường hợp change status user với không tìm thấy user
    @Test
    void UTCURA02() {
        Long userId = 200L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUserStatus(userId);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }
}
