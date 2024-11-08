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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserActiveTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    // Test trường hợp active user thành công
    @Test
    void UTCURA01() {
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
    void UTCURA02() {
        Long userId = 200L;

        when(userRepository.getUserById(userId)).thenReturn(Optional.empty());

        UserDto userDto = new UserDto();
        userDto.setId(userId);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.activeUser(userDto);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }

}
