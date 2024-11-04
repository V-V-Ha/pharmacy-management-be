package com.fu.pha.controller;

import com.fu.pha.dto.request.SaleOrder.SaleOrderRequestDto;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.exception.Message;
import com.fu.pha.service.SaleOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

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

    @GetMapping("/get-all-sale-order-paging")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<PageResponseModel<SaleOrderResponseDto>> getAllSaleOrderPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "", name = "orderType") OrderType orderType,
            @RequestParam(defaultValue = "", name = "paymentMethod") PaymentMethod paymentMethod,
            @RequestParam(defaultValue = "", name = "phoneNumber") String phoneNumber,
            @RequestParam(required = false, name = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false, name = "toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        Instant fromDateStart = fromDate != null ? fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant toDateEnd = toDate != null ? toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant() : null;

        Page<SaleOrderResponseDto> saleOrderResponseDto = saleOrderService.getAllSaleOrderPaging(page, size, orderType, paymentMethod, phoneNumber, fromDateStart, toDateEnd);
        PageResponseModel<SaleOrderResponseDto> response = PageResponseModel.<SaleOrderResponseDto>builder()
                .page(page)
                .size(size)
                .total(saleOrderResponseDto.getTotalElements())
                .listData(saleOrderResponseDto.getContent())
                .build();
        return ResponseEntity.ok(response);
    }


}
