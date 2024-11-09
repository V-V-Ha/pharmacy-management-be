package com.fu.pha.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fu.pha.dto.request.Payment.CreatePaymentLinkRequestBody;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.service.PaymentService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import java.util.*;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PayOS payOS;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${payment.payOS.checkSum_key}")
    private String checksumKey;

    public PaymentServiceImpl(PayOS payOS) {
        this.payOS = payOS;
    }

    @Override
    public ObjectNode createPaymentLink(CreatePaymentLinkRequestBody requestBody) {
        ObjectNode response = objectMapper.createObjectNode();
        try {
            String description = requestBody.getDescription();
            String returnUrl = requestBody.getReturnUrl();
            String cancelUrl = requestBody.getCancelUrl();
            int totalAmount = requestBody.getTotalAmount();

            // Generate unique order code
            String currentTimeString = String.valueOf(new Date().getTime());
            long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));

            // Chỉ cần truyền thông tin về số tiền và các URL
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .description(description)
                    .amount(totalAmount)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .build();


            // Tạo đối tượng dữ liệu thanh toán
            Map<String, String> paymentDataMap = new HashMap<>();
            paymentDataMap.put("amount", String.valueOf(totalAmount));
            paymentDataMap.put("orderCode", String.valueOf(orderCode));
            paymentDataMap.put("description", description);
            paymentDataMap.put("returnUrl", returnUrl);
            paymentDataMap.put("cancelUrl", cancelUrl);

            // Tạo chữ ký từ dữ liệu
            String signature = generateSignature(paymentDataMap);

            // Thêm chữ ký vào dữ liệu thanh toán
            paymentData.setSignature(signature);

            // Gửi yêu cầu tạo link thanh toán
            CheckoutResponseData data = payOS.createPaymentLink(paymentData);

            response.put("error", 0);
            response.put("message", Message.CREATE_SUCCESS);
            response.set("data", objectMapper.valueToTree(data));
            response.set("signature", objectMapper.valueToTree(signature));
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", -1);
            response.put("message", Message.CREATE_FAILED);
            response.set("data", null);
        }
        return response;
    }


    @Override
    public ObjectNode getOrderById(long orderId) {
        ObjectNode response = objectMapper.createObjectNode();
        try {
            PaymentLinkData order = payOS.getPaymentLinkInformation(orderId);

            response.put("error", 0);
            response.put("message", "ok");
            response.set("data", objectMapper.valueToTree(order));
        } catch (Exception e) {
            response.put("error", -1);
            response.put("message", Message.OTHER_ERROR);
            response.set("data", null);
        }
        return response;
    }

    @Override
    public ObjectNode cancelOrder(int orderId) {
        ObjectNode response = objectMapper.createObjectNode();
        try {
            PaymentLinkData order = payOS.cancelPaymentLink(orderId, null);

            response.put("error", 0);
            response.put("message", "ok");
            response.set("data", objectMapper.valueToTree(order));
        } catch (Exception e) {
            response.put("error", -1);
            response.put("message", Message.OTHER_ERROR);
            response.set("data", null);
        }
        return response;
    }

    @Override
    public ObjectNode confirmWebhook(Map<String, String> requestBody) {
        ObjectNode response = objectMapper.createObjectNode();
        try {
            // Lấy dữ liệu từ requestBody
            String transaction = requestBody.get("transaction");
            String transactionSignature = requestBody.get("transactionSignature");

            // Chuyển đổi transaction string thành map các cặp key-value
            Map<String, String> transactionData = objectMapper.readValue(transaction, Map.class);

            // Tạo chữ ký từ dữ liệu nhận được
            String generatedSignature = generateSignature(transactionData);

            // Kiểm tra chữ ký nếu không hợp lệ
            if (!generatedSignature.equals(transactionSignature)) {
                response.put("error", -1);
                response.put("message", "Invalid signature");
                response.set("data", null);
                return response;
            }

            // Nếu chữ ký hợp lệ, xử lý tiếp thông tin thanh toán
            String webhookResponse = payOS.confirmWebhook(requestBody.get("webhookUrl"));

            response.put("error", 0);
            response.put("message", "ok");
            response.set("data", objectMapper.valueToTree(webhookResponse));
        } catch (Exception e) {
            response.put("error", -1);
            response.put("message", Message.OTHER_ERROR);
            response.set("data", null);
        }
        return response;
    }


    @Override
    public void checkout(CreatePaymentLinkRequestBody requestBody, HttpServletResponse httpServletResponse) {
        try {
            // Lấy thông tin từ requestBody
            final String description = requestBody.getDescription();
            final String returnUrl = requestBody.getReturnUrl();
            final String cancelUrl = requestBody.getCancelUrl();
            final int totalAmount = requestBody.getTotalAmount();

            // Gen order code
            String currentTimeString = String.valueOf(new Date().getTime());
            long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));

            // Tạo đối tượng PaymentData từ request
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(totalAmount)
                    .description(description)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .build();

            // Tạo đối tượng dữ liệu thanh toán
            Map<String, String> paymentDataMap = new HashMap<>();
            paymentDataMap.put("amount", String.valueOf(totalAmount));
            paymentDataMap.put("orderCode", String.valueOf(orderCode));
            paymentDataMap.put("description", description);
            paymentDataMap.put("returnUrl", returnUrl);
            paymentDataMap.put("cancelUrl", cancelUrl);

            // Tạo chữ ký từ dữ liệu
            String signature = generateSignature(paymentDataMap);

            // Thêm chữ ký vào dữ liệu thanh toán
            paymentData.setSignature(signature);

            // Gửi yêu cầu tạo link thanh toán
            CheckoutResponseData data = payOS.createPaymentLink(paymentData);

            // Lấy checkoutUrl và trả về
            String checkoutUrl = data.getCheckoutUrl();
            httpServletResponse.setHeader("Location", checkoutUrl);
            httpServletResponse.setStatus(302);
        } catch (Exception e) {
            throw new BadRequestException(Message.OTHER_ERROR);
        }
    }

    public String generateSignature(Map<String, String> data) {
        try {
            // Sắp xếp các trường dữ liệu theo tên key
            List<String> keys = new ArrayList<>(data.keySet());
            Collections.sort(keys);

            // Tạo chuỗi dữ liệu từ các key và value
            StringBuilder transactionStr = new StringBuilder();
            for (String key : keys) {
                String value = data.get(key);
                transactionStr.append(key).append('=').append(value);
                transactionStr.append('&');
            }

            // Loại bỏ dấu "&" cuối cùng
            if (transactionStr.length() > 0) {
                transactionStr.deleteCharAt(transactionStr.length() - 1);
            }

            // Tạo chữ ký HMAC SHA256
            return new HmacUtils("HmacSHA256", checksumKey).hmacHex(transactionStr.toString());
        } catch (Exception e) {
            throw new BadRequestException(Message.OTHER_ERROR);
        }
    }


}
