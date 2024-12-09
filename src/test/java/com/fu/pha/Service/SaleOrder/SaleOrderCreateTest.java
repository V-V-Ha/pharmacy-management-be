package com.fu.pha.Service.SaleOrder;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.SaleOrder.SaleOrderItemRequestDto;
import com.fu.pha.dto.request.SaleOrder.SaleOrderRequestDto;
import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.*;
import com.fu.pha.service.impl.SaleOrderServiceImpl; // Sử dụng lớp triển khai cụ thể
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private SaleOrderItemBatchRepository saleOrderItemBatchRepository;
    @Mock
    private GenerateCode generateCode;

    @InjectMocks
    private SaleOrderServiceImpl saleOrderService; // Sử dụng lớp triển khai cụ thể

    // Helper method để tạo SaleOrderItemRequestDto
    private SaleOrderItemRequestDto createSaleOrderItemRequestDto(Long productId, int quantity, double unitPrice, double discount, int conversionFactor, String dosage, String unit) {
        SaleOrderItemRequestDto dto = new SaleOrderItemRequestDto();
        dto.setProductId(productId);
        dto.setQuantity(quantity);
        dto.setUnitPrice(unitPrice);
        dto.setDiscount(discount);
        dto.setConversionFactor(conversionFactor);
        dto.setDosage(dosage);
        dto.setUnit(unit);
        return dto;
    }
    // Test tạo đơn hàng thành công
    @Test
    void testCreateSaleOrder_Success() {
        // Setup mock data
        SaleOrderRequestDto saleOrderRequestDto = new SaleOrderRequestDto();
        saleOrderRequestDto.setCustomerId(1L);
        saleOrderRequestDto.setUserId(1L);
        saleOrderRequestDto.setOrderType(OrderType.NORMAL);
        saleOrderRequestDto.setPaymentMethod(PaymentMethod.CASH);
        saleOrderRequestDto.setTotalAmount(1000.0); // Chỉnh sửa tổng tiền để khớp với tính toán

        // Thiết lập danh sách SaleOrderItemRequestDto
        saleOrderRequestDto.setSaleOrderItems(Arrays.asList(
                createSaleOrderItemRequestDto(1L, 10, 100.0, 0, 1, "pill", "tablet")
        ));

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

        // Mock save method for SaleOrder to set id on the passed object
        when(saleOrderRepository.save(any(SaleOrder.class))).thenAnswer(invocation -> {
            SaleOrder order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Mock save method for SaleOrderItem to set id on the passed object
        when(saleOrderItemRepository.save(any(SaleOrderItem.class))).thenAnswer(invocation -> {
            SaleOrderItem item = invocation.getArgument(0);
            item.setId(1L);
            return item;
        });

        // Mock processOrderInventory dependencies
        ImportItem importItem = new ImportItem();
        importItem.setId(1L);
        importItem.setProduct(product);
        importItem.setRemainingQuantity(50);
        importItem.setExpiryDate(null); // Không hết hạn

        when(importItemRepository.findByProductIdOrderByCreateDateAsc(1L)).thenReturn(Arrays.asList(importItem));

        // Mock save method for ImportItem to set id
        when(importItemRepository.save(any(ImportItem.class))).thenAnswer(invocation -> {
            ImportItem item = invocation.getArgument(0);
            item.setId(1L);
            return item;
        });

        // Mock save method for SaleOrderItemBatch to set id
        when(saleOrderItemBatchRepository.save(any(SaleOrderItemBatch.class))).thenAnswer(invocation -> {
            SaleOrderItemBatch batch = invocation.getArgument(0);
            batch.setId(1L);
            return batch;
        });

        // Mock save method for Product to set id (nếu cần thiết)
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            // Giữ nguyên id
            return p;
        });

        // Test the method
        int saleOrderId = saleOrderService.createSaleOrder(saleOrderRequestDto);

        // Verify interactions and results
        verify(customerRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(1L);
        verify(saleOrderRepository, times(1)).getLastInvoiceNumber();
        verify(saleOrderRepository, times(2)).save(any(SaleOrder.class)); // Lần đầu để tạo và lần sau để cập nhật totalAmount
        verify(saleOrderItemRepository, times(1)).save(any(SaleOrderItem.class));
        verify(importItemRepository, times(1)).findByProductIdOrderByCreateDateAsc(1L);
        verify(importItemRepository, times(1)).save(any(ImportItem.class));
        verify(saleOrderItemBatchRepository, times(1)).save(any(SaleOrderItemBatch.class));
        verify(productRepository, times(1)).save(any(Product.class));

        assertEquals(1, saleOrderId);
    }


    // Test khi không tìm thấy khách hàng
    @Test
    void testCreateSaleOrder_CustomerNotFound() {
        // Arrange
        SaleOrderRequestDto requestDto = new SaleOrderRequestDto();
        requestDto.setCustomerId(1L);
        requestDto.setUserId(1L);
        requestDto.setOrderType(OrderType.NORMAL);
        requestDto.setPaymentMethod(PaymentMethod.CASH);
        requestDto.setTotalAmount(1000.0);

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            saleOrderService.createSaleOrder(requestDto);
        });

        assertEquals(Message.CUSTOMER_NOT_FOUND, exception.getMessage());
    }

    // Test khi không tìm thấy người dùng
    @Test
    void testCreateSaleOrder_UserNotFound() {
        // Arrange
        SaleOrderRequestDto requestDto = new SaleOrderRequestDto();
        requestDto.setCustomerId(1L);
        requestDto.setUserId(1L);
        requestDto.setOrderType(OrderType.NORMAL);
        requestDto.setPaymentMethod(PaymentMethod.CASH);
        requestDto.setTotalAmount(1000.0);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer()));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            saleOrderService.createSaleOrder(requestDto);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }

    // Test khi không tìm thấy bác sĩ
    @Test
    void testCreateSaleOrder_DoctorNotFound() {
        // Arrange
        SaleOrderRequestDto requestDto = new SaleOrderRequestDto();
        requestDto.setCustomerId(1L);
        requestDto.setUserId(1L);
        requestDto.setDoctorId(1L); // Giả sử bạn có trường doctorId trong DTO
        requestDto.setOrderType(OrderType.PRESCRIPTION);
        requestDto.setPaymentMethod(PaymentMethod.CASH);
        requestDto.setTotalAmount(1000.0);
        requestDto.setSaleOrderItems(Arrays.asList(
                createSaleOrderItemRequestDto(1L, 10, 100.0, 0.1, 1, "pill", "tablet")
        ));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            saleOrderService.createSaleOrder(requestDto);
        });

        assertEquals(Message.DOCTOR_NOT_FOUND, exception.getMessage());
    }

    // Test khi không tìm thấy sản phẩm
    @Test
    void testCreateSaleOrder_ProductNotFound() {
        // Arrange
        SaleOrderRequestDto saleOrderRequestDto = new SaleOrderRequestDto();
        saleOrderRequestDto.setCustomerId(1L);
        saleOrderRequestDto.setUserId(1L);
        saleOrderRequestDto.setOrderType(OrderType.NORMAL);
        saleOrderRequestDto.setPaymentMethod(PaymentMethod.CASH);
        saleOrderRequestDto.setTotalAmount(1000.0);
        saleOrderRequestDto.setSaleOrderItems(Arrays.asList(
                createSaleOrderItemRequestDto(1L, 10, 100.0, 0.1, 1, "pill", "tablet")
        ));

        Customer customer = new Customer();
        customer.setId(1L);

        User user = new User();
        user.setId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            saleOrderService.createSaleOrder(saleOrderRequestDto);
        });
    }

    // Test khi tổng tiền không khớp
    @Test
    void testCreateSaleOrder_TotalAmountMismatch() {
        // Arrange
        SaleOrderRequestDto saleOrderRequestDto = new SaleOrderRequestDto();
        saleOrderRequestDto.setCustomerId(1L);
        saleOrderRequestDto.setUserId(1L);
        saleOrderRequestDto.setOrderType(OrderType.NORMAL);
        saleOrderRequestDto.setPaymentMethod(PaymentMethod.CASH);
        saleOrderRequestDto.setTotalAmount(999.0); // Tổng tiền không khớp
        saleOrderRequestDto.setSaleOrderItems(Arrays.asList(
                createSaleOrderItemRequestDto(1L, 10, 100.0, 0.1, 1, "pill", "tablet")
        ));

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

        // Mock saleOrderRepository.save() để set id
        when(saleOrderRepository.save(any(SaleOrder.class))).thenAnswer(invocation -> {
            SaleOrder order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Mock saleOrderItemRepository.save() để set id
        when(saleOrderItemRepository.save(any(SaleOrderItem.class))).thenAnswer(invocation -> {
            SaleOrderItem item = invocation.getArgument(0);
            item.setId(1L);
            return item;
        });

        // Mock processOrderInventory dependencies (không cần thiết vì exception được ném trước khi gọi)
        ImportItem importItem = new ImportItem();
        importItem.setId(1L);
        importItem.setProduct(product);
        importItem.setRemainingQuantity(50);
        importItem.setExpiryDate(null); // Không hết hạn

        when(importItemRepository.findByProductIdOrderByCreateDateAsc(1L)).thenReturn(Arrays.asList(importItem));
        when(importItemRepository.save(any(ImportItem.class))).thenAnswer(invocation -> {
            ImportItem item = invocation.getArgument(0);
            item.setId(1L);
            return item;
        });

        when(saleOrderItemBatchRepository.save(any(SaleOrderItemBatch.class))).thenAnswer(invocation -> {
            SaleOrderItemBatch batch = invocation.getArgument(0);
            batch.setId(1L);
            return batch;
        });

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            return p;
        });

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            saleOrderService.createSaleOrder(saleOrderRequestDto);
        });

        assertEquals(Message.TOTAL_AMOUNT_NOT_MATCH, exception.getMessage()); // Đảm bảo Message.TOTAL_AMOUNT_NOT_MATCH là "Tổng tiền không khớp"
    }
}
