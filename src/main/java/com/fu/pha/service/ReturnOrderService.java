package com.fu.pha.service;

import com.fu.pha.dto.request.ReturnOrderRequestDto;
import com.fu.pha.dto.response.ReturnOrderResponseDto;
import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import org.springframework.data.domain.Page;

import java.time.Instant;

public interface ReturnOrderService {
    void createReturnOrder(ReturnOrderRequestDto returnOrderRequestDto);

    void updateReturnOrder(Long returnOrderId, ReturnOrderRequestDto returnOrderRequestDto);

    SaleOrderResponseDto getSaleOrderByInvoiceNumber(String invoiceNumber);

    ReturnOrderResponseDto getReturnOrderById(Long returnOrderId);

    Page<ReturnOrderResponseDto> getAllReturnOrderPaging(int size, int index, String invoiceNumber, Instant fromDate, Instant toDate);
}
