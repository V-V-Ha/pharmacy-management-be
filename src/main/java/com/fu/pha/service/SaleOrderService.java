package com.fu.pha.service;

import com.fu.pha.dto.request.SaleOrder.SaleOrderRequestDto;
import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;

public interface SaleOrderService {
    void createSaleOrder(SaleOrderRequestDto saleOrderRequestDto);

    void updateSaleOrder(Long saleOrderId, SaleOrderRequestDto saleOrderRequestDto);

    SaleOrderResponseDto getSaleOrderById(Long saleOrderId);
}
