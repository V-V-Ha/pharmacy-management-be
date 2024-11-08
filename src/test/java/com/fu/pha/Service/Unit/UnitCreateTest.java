package com.fu.pha.Service.Unit;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.entity.Unit;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.repository.UnitRepository;
import com.fu.pha.service.impl.UnitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UnitCreateTest {

    @InjectMocks
    private UnitServiceImpl unitService;

    @Mock
    private UnitRepository unitRepository;

    private UnitDto unitDto;
    private Unit unit;

    @BeforeEach
    void setUpCreate() {
        unitDto = new UnitDto();
        unitDto.setUnitName("Hộp");

        unit = new Unit();
        unit.setUnitName("Hộp");
    }

    //test trường hợp tạo unit thành công
    @Test
    void UTCUNC01() {
        when(unitRepository.findByUnitName("Hộp")).thenReturn(null);

        unitService.createUnit(unitDto);

        verify(unitRepository).save(any(Unit.class));
    }

    //test trường hợp tạo unit với tên null
    @Test
    void UTCUNC02() {
        unitDto.setUnitName("");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            unitService.createUnit(unitDto);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }


    //test trường hợp tạo unit đã tồn tại
    @Test
    void UTCUNC03() {
        when(unitRepository.findByUnitName("Hộp")).thenReturn(unit);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            unitService.createUnit(unitDto);
        });

        assertEquals(Message.UNIT_EXIST, exception.getMessage());
    }

}
