package com.fu.pha.Service.Unit;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.entity.Unit;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.UnitRepository;
import com.fu.pha.service.impl.UnitServiceImpl;
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
public class UnitChangeStatusTest {

    @InjectMocks
    private UnitServiceImpl unitService;

    @Mock
    private UnitRepository unitRepository;

    private UnitDto unitDto;
    private Unit unit;

    //test trường hợp change status unit thành công
    @Test
    void UTCUNCS01() {
        Long unitId = 1L;
        Unit unit = new Unit();
        unit.setId(unitId);
        unit.setStatus(Status.ACTIVE);

        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));

        unitService.updateUnitStatus(unitId);

        assertTrue(unit.getStatus() == Status.INACTIVE);
        verify(unitRepository).save(unit);
    }

    //test trường hợp change status unit không tồn tại
    @Test
    void UTCUNCS02() {
        when(unitRepository.findById(200L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            unitService.updateUnitStatus(200L);
        });
        assertEquals(Message.UNIT_NOT_FOUND, exception.getMessage());
    }
}
