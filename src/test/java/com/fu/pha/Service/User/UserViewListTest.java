package com.fu.pha.Service.User;

import com.fu.pha.dto.request.UserDto;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.Gender;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.UserRepository;
import com.fu.pha.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserViewListTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private Pageable pageable;

    @BeforeEach
    public void setUp() {
        pageable = PageRequest.of(0, 10);
    }

    // Test case: Found users
    @Test
    public void UTCURL01() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setFullName("Minh Hiếu");
        Page<UserDto> expectedPage = new PageImpl<>(List.of(userDto));
        when(userRepository.getAllUserPaging("Minh Hiếu", null, Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act
        Page<UserDto> result = userService.getAllUserPaging(0, 10, "Minh Hiếu", null, "ACTIVE");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(userRepository).getAllUserPaging("Minh Hiếu", null, Status.ACTIVE, pageable);
    }

    // Test case: No users found
    @Test
    public void UTCURL02() {
        // Arrange
        Page<UserDto> expectedPage = Page.empty();
        when(userRepository.getAllUserPaging("Hà", null, Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                userService.getAllUserPaging(0, 10, "Hà", null, "ACTIVE"));
        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
        verify(userRepository).getAllUserPaging("Hà", null, Status.ACTIVE, pageable);
    }

    // Test case: Invalid role value (not found)
    @Test
    public void UTCURL03() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                userService.getAllUserPaging(0, 10, "Minh Hiếu", "INVALID_ROLE", "ACTIVE"));
        assertEquals(Message.ROLE_NOT_FOUND, exception.getMessage());
    }

    // Test case: Invalid status value (not found)
    @Test
    public void UTCURL04() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                userService.getAllUserPaging(0, 10, "Minh Hiếu", null, "INVALID_STATUS"));
        assertEquals(Message.STATUS_NOT_FOUND, exception.getMessage());
    }

    // Test case: role is null
    @Test
    public void UTCURL05() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setFullName("Minh Hiếu");
        Page<UserDto> expectedPage = new PageImpl<>(List.of(userDto));
        when(userRepository.getAllUserPaging("Minh Hiếu", null, Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act
        Page<UserDto> result = userService.getAllUserPaging(0, 10, "Minh Hiếu", null, "ACTIVE");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(userRepository).getAllUserPaging("Minh Hiếu", null, Status.ACTIVE, pageable);
    }

    // Test case: status is null
    @Test
    public void UTCURL06() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setFullName("Minh Hiếu");
        Page<UserDto> expectedPage = new PageImpl<>(List.of(userDto));
        when(userRepository.getAllUserPaging("Minh Hiếu", null, null, pageable)).thenReturn(expectedPage);

        // Act
        Page<UserDto> result = userService.getAllUserPaging(0, 10, "Minh Hiếu", null, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(userRepository).getAllUserPaging("Minh Hiếu", null, null, pageable);
    }

    @Test
    public void UTCURL07() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setFullName("Minh Hiếu");
        Page<UserDto> expectedPage = new PageImpl<>(List.of(userDto));
        when(userRepository.getAllUserPaging(null, null, Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act
        Page<UserDto> result = userService.getAllUserPaging(0, 10, null, null, "ACTIVE");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(userRepository).getAllUserPaging(null, null, Status.ACTIVE, pageable);
    }
}
