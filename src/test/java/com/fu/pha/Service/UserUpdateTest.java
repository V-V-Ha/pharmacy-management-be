package com.fu.pha.Service;

import com.fu.pha.dto.request.RoleDto;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.entity.Role;
import com.fu.pha.entity.User;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.Gender;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.RoleRepository;
import com.fu.pha.repository.UserRepository;
import com.fu.pha.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserUpdateTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;

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
    void UTCUUR01() {
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
    void UTCUUR02() {
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
    void UTCUUR03() {
        userDto.setFullName("Vũ Văn H123");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.INVALID_NAME, exception.getMessage());
    }

    // Test trường hợp cập nhật user với full name là null
    @Test
    void UTCUUR04() {
        userDto.setFullName("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với email không hợp lệ
    @Test
    void UTCUUR05() {
        userDto.setEmail("vuha2002@gmail.co");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.INVALID_GMAIL, exception.getMessage());
    }

    // Test trường hợp cập nhật user với email là null
    @Test
    void UTCUUR06() {
        userDto.setEmail("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với phone không hợp lệ
    @Test
    void UTCUUR07() {
        userDto.setPhone("098765432a");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.INVALID_PHONE, exception.getMessage());
    }

    // Test trường hợp cập nhật user với phone là null
    @Test
    void UTCUUR08() {
        userDto.setPhone("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với không đủ 18 tuổi
    @Test
    void UTCUUR09() {
        userDto.setDob(Instant.now().minus(Duration.ofDays(365 * 17)));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.INVALID_AGE, exception.getMessage());
    }

    // Test trường hợp cập nhật user với dob là null
    @Test
    void UTCUUR10() {
        userDto.setDob(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với address không hợp lệ
    @Test
    void UTCUUR11() {
        userDto.setAddress("Ngõ 5, Đường Láng");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.INVALID_ADDRESS, exception.getMessage());
    }

    // Test trường hợp cập nhật user với address là null
    @Test
    void UTCUUR12() {
        userDto.setAddress("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với gender là null
    @Test
    void UTCUUR13() {
        userDto.setGender(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với cic không hợp lệ
    @Test
    void UTCUUR14() {
        userDto.setCic("1234567890123");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.INVALID_CCCD, exception.getMessage());
    }

    // Test trường hợp cập nhật user với cic là null
    @Test
    void UTCUUR15() {
        userDto.setCic("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với username không hợp lệ
    @Test
    void UTCUUR16() {
        userDto.setUsername("havv");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.INVALID_USERNAME_C, exception.getMessage());
    }

    // Test trường hợp cập nhật user với username là null
    @Test
    void UTCUUR17() {
        userDto.setUsername("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp cập nhật user với role là null
    @Test
    void UTCUUR18() {
        userDto.setRolesDto(new HashSet<>());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }
}
