package com.fu.pha.Service.User;

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
import com.fu.pha.service.EmailService;
import com.fu.pha.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserCreateTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;

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
    void UTCCUR01() {
        // Giả lập tìm kiếm các thông tin của người dùng, đảm bảo không bị trùng lặp
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.getUserByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.getUserByCic(userDto.getCic())).thenReturn(Optional.empty());
        when(userRepository.getUserByPhone(userDto.getPhone())).thenReturn(Optional.empty());
        when(encoder.encode(anyString())).thenReturn("encodedPassword");

        // Giả lập tìm kiếm role
        Role role = new Role();
        role.setName(ERole.ROLE_PRODUCT_OWNER);
        when(roleRepository.findByName(ERole.ROLE_PRODUCT_OWNER)).thenReturn(Optional.of(role));

        // Giả lập gửi email
        doNothing().when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());

        // Gọi phương thức tạo người dùng
        userService.createUser(userDto, null);

        // Xác minh rằng phương thức save đã được gọi với bất kỳ đối tượng User nào
        verify(userRepository).save(any(User.class));
    }

    // Test trường hợp tạo user với full name không hợp lệ
    @Test
    void UTCCUR02() {
        userDto.setFullName("Vũ Văn H123");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.INVALID_NAME, exception.getMessage());
    }

    // Test trường hợp tạo user với full name là null
    @Test
    void UTCCUR03() {
        userDto.setFullName("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp tạo user với email không hợp lệ
    @Test
    void UTCCUR04() {
        userDto.setEmail("vuha2002@gmail.co");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.INVALID_GMAIL, exception.getMessage());
    }

    // Test trường hợp tạo user với email là null
    @Test
    void UTCCUR05() {
        userDto.setEmail("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp tạo user với phone không hợp lệ
    @Test
    void UTCCUR06() {
        userDto.setPhone("'098765432a");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.INVALID_PHONE, exception.getMessage());
    }

    // Test trường hợp tạo user với phone là null
    @Test
    void UTCCUR07() {
        userDto.setPhone("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp tạo user với không đủ 18 tuổi
    @Test
    void UTCCUR08() {
        userDto.setDob(Instant.now().minus(Duration.ofDays(365 * 17)));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.INVALID_AGE, exception.getMessage());
    }

    // Test trường hợp tạo user với dob là null
    @Test
    void UTCCUR09() {
        userDto.setDob(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    @Test
    void UTCCUR10() {
        userDto.setAddress("Ngõ 5, Đường Láng");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.INVALID_ADDRESS, exception.getMessage());
    }

    // Test trường hợp tạo user với address là null
    @Test
    void UTCCUR11() {
        userDto.setAddress("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp tạo user với gender là null
    @Test
    void UTCCUR12() {
        userDto.setGender(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp tạo user với cic không hợp lệ
    @Test
    void UTCCUR13() {
        userDto.setCic("1234567890123");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.INVALID_CCCD, exception.getMessage());
    }

    // Test trường hợp tạo user với cic là null
    @Test
    void UTCCUR14() {
        userDto.setCic("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp tạo user với username không hợp lệ
    @Test
    void UTCCUR15() {
        userDto.setUsername("havv");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.INVALID_USERNAME_C, exception.getMessage());
    }

    // Test trường hợp tạo user với username là null
    @Test
    void UTCCUR16() {
        userDto.setUsername("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    // Test trường hợp tạo user với role là null
    @Test
    void UTCCUR17() {
        userDto.setRolesDto(new HashSet<>());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(userDto, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }
}
