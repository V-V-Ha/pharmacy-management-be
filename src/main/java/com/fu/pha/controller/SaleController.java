package com.fu.pha.controller;

import com.fu.pha.dto.request.SaleOrder.SaleOrderRequestDto;
import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.exception.Message;
import com.fu.pha.service.SaleOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/sale")
public class SaleController {

    @Autowired
    private SaleOrderService saleOrderService;

    @PostMapping("/create-sale-order")
    public ResponseEntity<String> createSaleOrder(@Valid @RequestBody SaleOrderRequestDto saleOrderRequestDto) {
        saleOrderService.createSaleOrder(saleOrderRequestDto);
        return ResponseEntity.ok(Message.CREATE_SUCCESS);
    }

    @PutMapping("/update-sale-order")
    public ResponseEntity<String> updateSaleOrder(@RequestParam Long saleOrderId, @Valid @RequestBody SaleOrderRequestDto saleOrderRequestDto) {
        saleOrderService.updateSaleOrder(saleOrderId, saleOrderRequestDto);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    @GetMapping("/get-sale-order")
    public ResponseEntity<SaleOrderResponseDto> getSaleOrderById(@RequestParam Long saleOrderId) {
        return ResponseEntity.ok(saleOrderService.getSaleOrderById(saleOrderId));
    }


}
