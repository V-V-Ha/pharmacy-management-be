package com.fu.pha.service;

import com.fu.pha.dto.request.SaleOrder.SaleOrderRequestDto;
import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentMethod;
import org.springframework.data.domain.Page;

import java.time.Instant;

public interface SaleOrderService {
    int createSaleOrder(SaleOrderRequestDto saleOrderRequestDto);

    void updateSaleOrder(Long saleOrderId, SaleOrderRequestDto saleOrderRequestDto);

    SaleOrderResponseDto getSaleOrderById(Long saleOrderId);

    void completePayment(long orderId);

    Page<SaleOrderResponseDto> getAllSaleOrderPaging(int size, int index, OrderType orderType, PaymentMethod paymentMethod, String invoiceNumber, Instant fromDate, Instant toDate);
}
