package com.fu.pha.Service.SaleOrder;

import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.entity.SaleOrder;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.SaleOrderRepository;
import com.fu.pha.service.SaleOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SaleOrderViewDetailTest {

    private SaleOrderRepository saleOrderRepository = mock(SaleOrderRepository.class);
    @Mock
    private SaleOrderService saleOrderService;
            ;

    @Test
    public void testGetSaleOrderById_Success() {
        // Arrange
        Long saleOrderId = 1L; // ID hợp lệ có trong cơ sở dữ liệu
        //SaleOrder saleOrder = new SaleOrder(saleOrderId, "Customer A", LocalDate.now(), 1000.0);
       // when(saleOrderRepository.findById(saleOrderId)).thenReturn(Optional.of(saleOrder));

        // Act
        SaleOrderResponseDto response = saleOrderService.getSaleOrderById(saleOrderId);

        // Assert
        assertNotNull(response);
        assertEquals(saleOrderId, response.getId());
     //   assertEquals("Customer A", response.getCustomerName());
        assertEquals(1000.0, response.getTotalAmount(), 0.01);
        verify(saleOrderRepository).findById(saleOrderId); // Kiểm tra repository đã được gọi
    }

    @Test
    public void testGetSaleOrderById_NotFound() {
        // Arrange
        Long saleOrderId = 999L; // ID không tồn tại trong cơ sở dữ liệu
        when(saleOrderRepository.findById(saleOrderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            saleOrderService.getSaleOrderById(saleOrderId);
        });
        verify(saleOrderRepository).findById(saleOrderId); // Kiểm tra repository đã được gọi
    }
}
