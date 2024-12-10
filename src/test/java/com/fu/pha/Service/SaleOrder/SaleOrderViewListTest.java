package com.fu.pha.Service.SaleOrder;

import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.SaleOrderRepository;
import com.fu.pha.service.impl.SaleOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SaleOrderViewListTest {

    @Mock
    private SaleOrderRepository saleOrderRepository;

    @InjectMocks
    private SaleOrderServiceImpl saleOrderService;

    private Pageable pageable;

    @BeforeEach
    public void setUp() {
        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void testGetAllSaleOrderPaging_NoDates() {
        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = LocalDate.now().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
        Page<SaleOrderResponseDto> page = new PageImpl<>(Collections.emptyList());

        when(saleOrderRepository.getListSaleOrderPagingWithoutDate(any(), any(), any(), eq(startOfDay), eq(endOfDay), eq(pageable)))
                .thenReturn(page);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            saleOrderService.getAllSaleOrderPaging(0, 10, OrderType.NORMAL, PaymentMethod.CASH, "INV123", null, null);
        });

        assertEquals(Message.SALE_ORDER_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testGetAllSaleOrderPaging_FromDateOnly() {
        Instant fromDate = Instant.now();
        Page<SaleOrderResponseDto> page = new PageImpl<>(Collections.emptyList());

        when(saleOrderRepository.getListSaleOrderPagingFromDate(any(), any(), any(), eq(fromDate), eq(pageable)))
                .thenReturn(page);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            saleOrderService.getAllSaleOrderPaging(0, 10, OrderType.NORMAL, PaymentMethod.CASH, "INV123", fromDate, null);
        });

        assertEquals(Message.SALE_ORDER_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testGetAllSaleOrderPaging_ToDateOnly() {
        Instant toDate = Instant.now();
        Page<SaleOrderResponseDto> page = new PageImpl<>(Collections.emptyList());

        when(saleOrderRepository.getListSaleOrderPagingToDate(any(), any(), any(), eq(toDate), eq(pageable)))
                .thenReturn(page);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            saleOrderService.getAllSaleOrderPaging(0, 10, OrderType.NORMAL, PaymentMethod.CASH, "INV123", null, toDate);
        });

        assertEquals(Message.SALE_ORDER_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testGetAllSaleOrderPaging_FromAndToDate() {
        Instant fromDate = Instant.now();
        Instant toDate = Instant.now();
        Page<SaleOrderResponseDto> page = new PageImpl<>(Collections.emptyList());

        when(saleOrderRepository.getListSaleOrderPaging(any(), any(), any(), eq(fromDate), eq(toDate), eq(pageable)))
                .thenReturn(page);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            saleOrderService.getAllSaleOrderPaging(0, 10, OrderType.NORMAL, PaymentMethod.CASH, "INV123", fromDate, toDate);
        });

        assertEquals(Message.SALE_ORDER_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testGetAllSaleOrderPaging_Success() {
        Instant fromDate = Instant.now();
        Instant toDate = Instant.now();
        SaleOrderResponseDto saleOrderResponseDto = new SaleOrderResponseDto();
        Page<SaleOrderResponseDto> page = new PageImpl<>(Collections.singletonList(saleOrderResponseDto));

        when(saleOrderRepository.getListSaleOrderPaging(any(), any(), any(), eq(fromDate), eq(toDate), eq(pageable)))
                .thenReturn(page);

        Page<SaleOrderResponseDto> result = saleOrderService.getAllSaleOrderPaging(0, 10, OrderType.NORMAL, PaymentMethod.CASH, "INV123", fromDate, toDate);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }
}
