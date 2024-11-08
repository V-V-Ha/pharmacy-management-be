package com.fu.pha.Service.Unit;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.entity.Unit;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.UnitRepository;
import com.fu.pha.service.impl.UnitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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
public class UnitUpdateTest {

    @InjectMocks
    private UnitServiceImpl unitService;

    @Mock
    private UnitRepository unitRepository;

    private UnitDto unitDto;
    private Unit unit;

    @BeforeEach
    void setUpUpdate() {
        unitDto = new UnitDto();
        unitDto.setId(1L);
        unitDto.setUnitName("Hộp");

        unit = new Unit();
        unit.setId(1L);
        unit.setUnitName("Hộp");
    }

    //test trường hợp cập nhật unit thành công
    @Test
    void UTCUNU01() {
        // Giả lập rằng đơn vị tồn tại
        when(unitRepository.findById(unitDto.getId())).thenReturn(Optional.of(unit));
        // Giả lập rằng không có đơn vị nào khác có tên trùng
        when(unitRepository.findByUnitName(unitDto.getUnitName())).thenReturn(null);

        // Gọi phương thức updateUnit
        unitService.updateUnit(unitDto);

        // Kiểm tra rằng tên và mô tả của đơn vị đã được cập nhật
        assertEquals("Hộp", unit.getUnitName());
        verify(unitRepository).save(unit);
    }

    //test trường hợp cập nhật unit với id không tồn tại
    @Test
    void UTCUNU02() {
        // Set the ID of unitDto to 123
        unitDto.setId(123L);

        // Simulate that no unit is found with the given ID
        when(unitRepository.findById(unitDto.getId())).thenReturn(Optional.empty());

        // Check that ResourceNotFoundException is thrown
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            unitService.updateUnit(unitDto);
        });

        // Validate the exception message
        assertEquals(Message.UNIT_NOT_FOUND, exception.getMessage());
    }

    //test trường hợp cập nhật unit với tên null
    @Test
    void UTCUNU03() {
        // Kiểm tra trường hợp unitName là chuỗi rỗng
        unitDto.setUnitName("");
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            unitService.updateUnit(unitDto);
        });
        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật unit với tên đã tồn tại
    @Test
    void UTCUNU04() {
        // Giả lập đơn vị hiện tại
        when(unitRepository.findById(unitDto.getId())).thenReturn(Optional.of(unit));

        // Tạo đơn vị khác với tên trùng để giả lập lỗi trùng tên
        Unit duplicateUnit = new Unit();
        duplicateUnit.setId(2L); // Khác ID với unitDto
        duplicateUnit.setUnitName("Hộp");

        // Giả lập rằng tên đơn vị đã tồn tại trong hệ thống với ID khác
        when(unitRepository.findByUnitName(unitDto.getUnitName())).thenReturn(duplicateUnit);

        // Kiểm tra ngoại lệ BadRequestException được ném ra
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            unitService.updateUnit(unitDto);
        });

        assertEquals(Message.UNIT_EXIST, exception.getMessage());
    }

}
