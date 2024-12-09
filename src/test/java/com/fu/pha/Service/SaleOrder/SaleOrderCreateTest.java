package com.fu.pha.Service.SaleOrder;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.SaleOrder.SaleOrderItemRequestDto;
import com.fu.pha.dto.request.SaleOrder.SaleOrderRequestDto;
import com.fu.pha.entity.Customer;
import com.fu.pha.entity.Product;
import com.fu.pha.entity.SaleOrder;
import com.fu.pha.entity.User;
import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.*;
import com.fu.pha.service.SaleOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SaleOrderCreateTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private SaleOrderRepository saleOrderRepository;
    @Mock
    private SaleOrderItemRepository saleOrderItemRepository;
    @Mock
    private ImportItemRepository importItemRepository;
    @Mock
    private GenerateCode generateCode;

    @InjectMocks
    private SaleOrderService saleOrderService;

    // Test tạo đơn hàng thành công
    @Test
    void testCreateSaleOrder_Success() {
        // Setup mock data
        SaleOrderRequestDto saleOrderRequestDto = new SaleOrderRequestDto();
        saleOrderRequestDto.setCustomerId(1L);
        saleOrderRequestDto.setUserId(1L);
        saleOrderRequestDto.setOrderType(OrderType.NORMAL);
        saleOrderRequestDto.setPaymentMethod(PaymentMethod.CASH);
      //  saleOrderRequestDto.setSaleOrderItems(Arrays.asList(new SaleOrderItemRequestDto(1L, 10, 100.0, 0.1, 1, "pill")));
        saleOrderRequestDto.setTotalAmount(1000.0);

        Customer customer = new Customer();
        customer.setId(1L);

        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setTotalQuantity(100);

        // Mock repository responses
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(saleOrderRepository.getLastInvoiceNumber()).thenReturn(null);
        when(generateCode.generateNewProductCode(anyString())).thenReturn("XB000001");

        // Test the method
        int saleOrderId = saleOrderService.createSaleOrder(saleOrderRequestDto);

        // Verify interactions and results
        verify(saleOrderRepository).save(any(SaleOrder.class));
        assertEquals(1, saleOrderId);
    }

    // Test khi không tìm thấy khách hàng
    @Test
    void testCreateSaleOrder_CustomerNotFound() {
        // Arrange
        SaleOrderRequestDto requestDto = new SaleOrderRequestDto();
        requestDto.setCustomerId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            saleOrderService.createSaleOrder(requestDto);
        });

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void testCreateSaleOrder_UserNotFound() {
        // Arrange
        SaleOrderRequestDto requestDto = new SaleOrderRequestDto();
        requestDto.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            saleOrderService.createSaleOrder(requestDto);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testCreateSaleOrder_DoctorNotFound() {
        // Arrange
        SaleOrderRequestDto requestDto = new SaleOrderRequestDto();
        requestDto.setDoctorId(1L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            saleOrderService.createSaleOrder(requestDto);
        });

        assertEquals("Doctor not found", exception.getMessage());
    }

    // Test khi không tìm thấy sản phẩm
    @Test
    void testCreateSaleOrder_ProductNotFound() {
        SaleOrderRequestDto saleOrderRequestDto = new SaleOrderRequestDto();
        saleOrderRequestDto.setCustomerId(1L);
        saleOrderRequestDto.setUserId(1L);
       // saleOrderRequestDto.setSaleOrderItems(Arrays.asList(new SaleOrderItemRequestDto(1L, 10, 100.0, 0.1, 1, "pill")));

        Customer customer = new Customer();
        customer.setId(1L);

        User user = new User();
        user.setId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            saleOrderService.createSaleOrder(saleOrderRequestDto);
        });
    }

    // Test khi tổng tiền không khớp
    @Test
    void testCreateSaleOrder_TotalAmountMismatch() {
        SaleOrderRequestDto saleOrderRequestDto = new SaleOrderRequestDto();
        saleOrderRequestDto.setCustomerId(1L);
        saleOrderRequestDto.setUserId(1L);
      //  saleOrderRequestDto.setSaleOrderItems(Arrays.asList(new SaleOrderItemRequestDto(1L, 1, 100.0, 0.0, 1, "pill")));
        saleOrderRequestDto.setTotalAmount(999.0); // Tổng tiền không khớp

        Customer customer = new Customer();
        customer.setId(1L);

        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setTotalQuantity(100);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> {
            saleOrderService.createSaleOrder(saleOrderRequestDto);
        });
    }

}
