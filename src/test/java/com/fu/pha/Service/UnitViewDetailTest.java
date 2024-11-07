package com.fu.pha.Service;

import com.fu.pha.entity.Unit;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.UnitRepository;
import com.fu.pha.service.impl.UnitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class UnitViewDetailTest {

    @InjectMocks
    private UnitServiceImpl unitService;

    @Mock
    private UnitRepository unitRepository;

    private Unit unit;

    @BeforeEach
    void setUp() {
        // Khởi tạo tất cả các mock đã đánh dấu bằng @Mock trong lớp này
        MockitoAnnotations.openMocks(this);

        // Khởi tạo đối tượng Unit để sử dụng trong test
        unit = new Unit();
        unit.setUnitName("Hộp");
    }

    //test trường hợp lấy unit theo id thành công
    @Test
    void UTCUNVD01() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));

        assertEquals("Hộp", unitService.getUnitById(1L).getUnitName());
    }

    //test trường hợp lấy unit theo id không tồn tại
    @Test
    void UTCUNVD02() {
        when(unitRepository.findById(200L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            unitService.getUnitById(200L);
        });

        assertEquals(Message.UNIT_NOT_FOUND, exception.getMessage());
    }

}
