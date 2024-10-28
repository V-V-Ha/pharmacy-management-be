package com.fu.pha.service;

import com.fu.pha.dto.request.SaleOrder.SaleOrderRequestDto;

public interface SaleOrderService {
    void createSaleOrder(SaleOrderRequestDto saleOrderRequestDto);
}
