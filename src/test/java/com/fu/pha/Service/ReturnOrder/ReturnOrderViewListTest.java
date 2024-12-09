package com.fu.pha.Service.ReturnOrder;

import com.fu.pha.dto.response.ReturnOrderResponseDto;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ReturnOrderRepository;
import com.fu.pha.service.impl.ReturnOrderServiceImpl;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReturnOrderViewListTest {

    @Mock
    private ReturnOrderRepository returnOrderRepository;

    @InjectMocks
    private ReturnOrderServiceImpl returnOrderService;

    private Pageable pageable;
    private ReturnOrderResponseDto returnOrderResponseDto;

    @BeforeEach
    public void setUp() {
        pageable = PageRequest.of(0, 10);
        returnOrderResponseDto = new ReturnOrderResponseDto();
    }

    @Test
    public void testGetAllReturnOrderPaging_NoDates() {
        Page<ReturnOrderResponseDto> page = new PageImpl<>(Collections.singletonList(returnOrderResponseDto));
        when(returnOrderRepository.getListReturnOrderPagingWithoutDate("INV123", pageable)).thenReturn(page);

        Page<ReturnOrderResponseDto> result = returnOrderService.getAllReturnOrderPaging(0, 10, "INV123", null, null);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testGetAllReturnOrderPaging_FromDateOnly() {
        Instant fromDate = Instant.now();
        Page<ReturnOrderResponseDto> page = new PageImpl<>(Collections.singletonList(returnOrderResponseDto));
        when(returnOrderRepository.getListReturnOrderPagingFromDate("INV123", fromDate, pageable)).thenReturn(page);

        Page<ReturnOrderResponseDto> result = returnOrderService.getAllReturnOrderPaging(0, 10, "INV123", fromDate, null);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testGetAllReturnOrderPaging_ToDateOnly() {
        Instant toDate = Instant.now();
        Page<ReturnOrderResponseDto> page = new PageImpl<>(Collections.singletonList(returnOrderResponseDto));
        when(returnOrderRepository.getListReturnOrderPagingToDate("INV123", toDate, pageable)).thenReturn(page);

        Page<ReturnOrderResponseDto> result = returnOrderService.getAllReturnOrderPaging(0, 10, "INV123", null, toDate);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testGetAllReturnOrderPaging_BothDates() {
        Instant fromDate = Instant.now();
        Instant toDate = Instant.now();
        Page<ReturnOrderResponseDto> page = new PageImpl<>(Collections.singletonList(returnOrderResponseDto));
        when(returnOrderRepository.getListReturnOrderPaging("INV123", fromDate, toDate, pageable)).thenReturn(page);

        Page<ReturnOrderResponseDto> result = returnOrderService.getAllReturnOrderPaging(0, 10, "INV123", fromDate, toDate);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testGetAllReturnOrderPaging_NotFound() {
        when(returnOrderRepository.getListReturnOrderPagingWithoutDate("INV123", pageable)).thenReturn(Page.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            returnOrderService.getAllReturnOrderPaging(0, 10, "INV123", null, null);
        });

        assertEquals(Message.RETURN_ORDER_NOT_FOUND, exception.getMessage());
    }
}
