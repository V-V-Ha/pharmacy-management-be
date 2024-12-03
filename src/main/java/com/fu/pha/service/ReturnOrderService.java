package com.fu.pha.service;

import com.fu.pha.dto.request.ReturnOrderRequestDto;
import com.fu.pha.dto.response.ReturnOrderResponseDto;
import com.fu.pha.dto.response.SaleOrderForReturnDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.time.Instant;

public interface ReturnOrderService {
    void createReturnOrder(ReturnOrderRequestDto returnOrderRequestDto);

    void updateReturnOrder(Long returnOrderId, ReturnOrderRequestDto returnOrderRequestDto);

    ReturnOrderResponseDto getReturnOrderById(Long returnOrderId);

    SaleOrderForReturnDto getSaleOrderForReturn(String invoiceNumber);

    Page<ReturnOrderResponseDto> getAllReturnOrderPaging(int size, int index, String invoiceNumber, Instant fromDate, Instant toDate);

    void exportReturnOrdersToExcel(HttpServletResponse response, Instant fromInstant, Instant toInstant) throws IOException;
}
