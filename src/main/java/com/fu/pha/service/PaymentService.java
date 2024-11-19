package com.fu.pha.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fu.pha.dto.request.Payment.CreatePaymentLinkRequestBody;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface PaymentService {
    ObjectNode createPaymentLink(CreatePaymentLinkRequestBody requestBody);

    ObjectNode getOrderById(long orderId);

    ObjectNode cancelOrder(int orderId);

    ObjectNode confirmWebhook(Map<String, String> requestBody);

    void checkout(CreatePaymentLinkRequestBody requestBody, HttpServletResponse httpServletResponse);
}
