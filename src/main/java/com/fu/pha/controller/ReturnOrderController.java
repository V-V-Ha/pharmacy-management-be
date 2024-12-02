package com.fu.pha.controller;


import com.fu.pha.dto.request.ReturnOrderRequestDto;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.dto.response.ReturnOrderResponseDto;
import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.dto.response.SaleOrderForReturnDto;
import com.fu.pha.exception.Message;
import com.fu.pha.service.ReturnOrderService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/return-order")
@Validated
public class ReturnOrderController {
    @Autowired
    private ReturnOrderService returnOrderService;

    @PostMapping("/create-return-order")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public ResponseEntity<String> createReturnOrder(@Valid @RequestBody ReturnOrderRequestDto returnOrderRequestDto) {
        returnOrderService.createReturnOrder(returnOrderRequestDto);
        return ResponseEntity.ok(Message.CREATE_SUCCESS);
    }

    @PutMapping("/update-return-order")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public ResponseEntity<String> updateReturnOrder(@Valid @RequestParam Long returnOrderId,@Valid @RequestBody ReturnOrderRequestDto returnOrderRequestDto) {
        returnOrderService.updateReturnOrder(returnOrderId, returnOrderRequestDto);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    @GetMapping("/get-return-order-by-id")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public ResponseEntity<ReturnOrderResponseDto> getReturnOrderById(@RequestParam Long returnOrderId) {
        return ResponseEntity.ok(returnOrderService.getReturnOrderById(returnOrderId));
    }

    @GetMapping("/get-all-return-order-paging")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public ResponseEntity<PageResponseModel<ReturnOrderResponseDto>> getAllReturnOrderPaging(
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                @RequestParam(defaultValue = "", name = "invoiceNumber") String invoiceNumber,
                                                                @RequestParam(required = false, name = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                                @RequestParam(required = false, name = "toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        Instant fromDateStart = fromDate != null ? fromDate.atStartOfDay(ZoneOffset.ofHours(7)).toInstant() : null;
        Instant toDateEnd = toDate != null ? toDate.atTime(23, 59, 59).atZone(ZoneOffset.ofHours(7)).toInstant() : null;

        Page<ReturnOrderResponseDto> returnOrderResponseDto = returnOrderService.getAllReturnOrderPaging(page, size, invoiceNumber, fromDateStart, toDateEnd);
        PageResponseModel<ReturnOrderResponseDto> response = PageResponseModel.<ReturnOrderResponseDto>builder()
                .page(page)
                .size(size)
                .total(returnOrderResponseDto.getTotalElements())
                .listData(returnOrderResponseDto.getContent())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export-excel-return-orders")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public void exportReturnOrdersToExcel(
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            HttpServletResponse response) throws IOException {

        // Convert LocalDate to Instant in UTC+7
        Instant fromDateStart = fromDate != null
                ? fromDate.atStartOfDay(ZoneOffset.ofHours(7)).toInstant()
                : null;

        Instant toDateEnd = toDate != null
                ? toDate.atTime(23, 59, 59).atZone(ZoneOffset.ofHours(7)).toInstant()
                : null;

        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Danh_sach_tra_hang.xlsx";
        response.setHeader(headerKey, headerValue);

        // Call the service to export data
        returnOrderService.exportReturnOrdersToExcel(response, fromDateStart, toDateEnd);
    }


    @GetMapping("/get-sale-order-for-return")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public ResponseEntity<SaleOrderForReturnDto> getSaleOrderForReturn(@RequestParam String invoiceNumber) {
        return ResponseEntity.ok(returnOrderService.getSaleOrderForReturn(invoiceNumber));
    }

}
