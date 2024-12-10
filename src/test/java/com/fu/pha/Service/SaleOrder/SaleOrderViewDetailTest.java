package com.fu.pha.Service.SaleOrder;

import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.entity.Customer;
import com.fu.pha.entity.SaleOrder;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.SaleOrderRepository;
import com.fu.pha.service.impl.SaleOrderServiceImpl; // Giả định đây là implementation của SaleOrderService
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SaleOrderViewDetailTest {

    @Mock
    private SaleOrderRepository saleOrderRepository;

    @InjectMocks
    private SaleOrderServiceImpl saleOrderService; // Sử dụng thực thể thực sự, không phải mock

    // Test case lấy thông tin đơn hàng không thành công
    @Test
    public void UTCSOVD02() {
        // Arrange
        Long saleOrderId = 999L; // ID không tồn tại trong cơ sở dữ liệu
        when(saleOrderRepository.findById(saleOrderId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            saleOrderService.getSaleOrderById(saleOrderId);
        }, "Expected ResourceNotFoundException to be thrown");

        assertEquals(Message.SALE_ORDER_NOT_FOUND, exception.getMessage(), "Exception message should match");
        verify(saleOrderRepository, times(1)).findById(saleOrderId); // Kiểm tra repository đã được gọi đúng cách
    }
}
