package com.fu.pha.controller;


import com.fu.pha.dto.request.ReturnOrderRequestDto;
import com.fu.pha.dto.response.ReturnOrderResponseDto;
import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.exception.Message;
import com.fu.pha.service.ReturnOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/return-order")
@Validated
public class ReturnOrderController {
    @Autowired
    private ReturnOrderService returnOrderService;

    @PostMapping("/create-return-order")
    public ResponseEntity<String> createReturnOrder(@Valid @RequestBody ReturnOrderRequestDto returnOrderRequestDto) {
        returnOrderService.createReturnOrder(returnOrderRequestDto);
        return ResponseEntity.ok(Message.CREATE_SUCCESS);
    }

    @PutMapping("/update-return-order")
    public ResponseEntity<String> updateReturnOrder(@Valid @RequestParam Long returnOrderId,@Valid @RequestBody ReturnOrderRequestDto returnOrderRequestDto) {
        returnOrderService.updateReturnOrder(returnOrderId, returnOrderRequestDto);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    @GetMapping("/get-sale-order-by-invoice-number")
    public ResponseEntity<SaleOrderResponseDto> getSaleOrderByInvoiceNumber(@RequestParam String invoiceNumber) {
        return ResponseEntity.ok(returnOrderService.getSaleOrderByInvoiceNumber(invoiceNumber));
    }

    @GetMapping("/get-return-order-by-id")
    public ResponseEntity<ReturnOrderResponseDto> getReturnOrderById(@RequestParam Long returnOrderId) {
        return ResponseEntity.ok(returnOrderService.getReturnOrderById(returnOrderId));
    }

}
