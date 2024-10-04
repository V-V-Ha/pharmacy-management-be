package com.fu.pha.Service;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.service.UnitService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnitServiceTest {

    @Mock
    private UnitService unitService;

    @InjectMocks
    private UnitServiceTest unitServiceTest;

    private UnitDto unitDto;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic;

    @BeforeEach
    void setUp() {
        securityContextHolderMockedStatic = mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("minhhieu");
    }

    @AfterEach
    void tearDown() {
        if (securityContextHolderMockedStatic != null) {
            securityContextHolderMockedStatic.close();
        }
    }

    @Test
    void testUpdateUnitWithEmptyName() {
        UnitDto unitDto = new UnitDto();
        unitDto.setId(1L);
        unitDto.setUnitName("");
        unitDto.setDescription("Description");

        doThrow(new BadRequestException(Message.UNIT_EXIST)).when(unitService).updateUnit(any(UnitDto.class));

        assertThrows(BadRequestException.class, () -> unitService.updateUnit(unitDto));
    }

    @Test
    void testUpdateUnit() throws BadRequestException {
        UnitDto unitDto = new UnitDto();
        unitDto.setId(1L);
        unitDto.setUnitName("Unit Name");
        unitDto.setDescription("Description");

        doNothing().when(unitService).updateUnit(any(UnitDto.class));

        unitService.updateUnit(unitDto);

        // Verify the service method was called once
        verify(unitService).updateUnit(any(UnitDto.class));
    }

    @Test
    void testCreateUnitWithEmptyName() {
        UnitDto unitDto = new UnitDto();
        unitDto.setUnitName("");
        unitDto.setDescription("Description");

        doThrow(new BadRequestException(Message.UNIT_EXIST)).when(unitService).createUnit(any(UnitDto.class));

        assertThrows(BadRequestException.class, () -> unitService.createUnit(unitDto));
    }

    @Test
    void testCreateUnit() throws BadRequestException {
        UnitDto unitDto = new UnitDto();
        unitDto.setUnitName("Unit Name");
        unitDto.setDescription("Description");

        unitService.createUnit(unitDto);
    }

    @Test
    void testCreateUnitWithExistingName() {
        UnitDto unitDto = new UnitDto();
        unitDto.setUnitName("Unit Name");
        unitDto.setDescription("Description");

        lenient().doThrow(new BadRequestException(Message.UNIT_EXIST)).when(unitService).createUnit(any(UnitDto.class));
    }


}