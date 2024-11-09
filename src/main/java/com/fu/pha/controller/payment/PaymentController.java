package com.fu.pha.controller.payment;

import com.fu.pha.dto.request.Payment.CreatePaymentLinkRequestBody;
import com.fu.pha.exception.Message;
import com.fu.pha.service.PaymentService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PayOS payOS;

    @Autowired
    PaymentService paymentService;

    public PaymentController(PayOS payOS) {
        super();
        this.payOS = payOS;

    }

    @PostMapping(path = "/payos_transfer_handler")
    public ResponseEntity<ObjectNode> payosTransferHandler(@RequestBody ObjectNode body)
            throws JsonProcessingException, IllegalArgumentException {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode response = objectMapper.createObjectNode();
        Webhook webhookBody = objectMapper.treeToValue(body, Webhook.class);

        try {
            // Init Response
            response.put("error", 0);
            response.put("message", "Webhook delivered");
            response.set("data", null);

            WebhookData data = payOS.verifyPaymentWebhookData(webhookBody);
            System.out.println(data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", -1);
            response.put("message", e.getMessage());
            response.set("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/create-payment-link")
    public ResponseEntity<ObjectNode> createPaymentLink(@RequestBody CreatePaymentLinkRequestBody body) {
        return ResponseEntity.ok(paymentService.createPaymentLink(body));
    }

    @GetMapping("/get-order-by-id")
    public ResponseEntity<ObjectNode> getOrderById(@RequestParam Long orderId) {
        return ResponseEntity.ok(paymentService.getOrderById(orderId));
    }

    @PutMapping("/cancel-order")
    public ResponseEntity<ObjectNode> cancelOrder(@RequestParam int orderId) {
        return ResponseEntity.ok(paymentService.cancelOrder(orderId));
    }

    @PostMapping("/confirm-webhook")
    public ResponseEntity<ObjectNode> confirmWebhook(@RequestBody Map<String, String> requestBody) {
        return ResponseEntity.ok(paymentService.confirmWebhook(requestBody));
    }

    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(@RequestBody CreatePaymentLinkRequestBody requestBody, HttpServletResponse httpServletResponse) {
        paymentService.checkout(requestBody, httpServletResponse);
        return ResponseEntity.ok(Message.CREATE_SUCCESS);
    }









}
