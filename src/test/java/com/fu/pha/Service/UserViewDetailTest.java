package com.fu.pha.Service;

import com.fu.pha.dto.request.UserDto;
import com.fu.pha.entity.User;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserViewDetailTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    // Test trường hợp view user detail thành công
    @Test
    void UTCURVD01() {
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
    void UTCURVD02() {
        Long userId = 200L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.viewDetailUser(userId);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }

}
