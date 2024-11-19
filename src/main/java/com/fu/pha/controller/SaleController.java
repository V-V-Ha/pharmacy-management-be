package com.fu.pha.controller;

import com.fu.pha.dto.request.SaleOrder.SaleOrderRequestDto;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.dto.response.CustomerDTOResponse;
import com.fu.pha.dto.response.DoctorDTOResponse;
import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.entity.SaleOrder;
import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.SaleOrderRepository;
import com.fu.pha.service.CustomerService;
import com.fu.pha.service.DoctorService;
import com.fu.pha.service.InvoiceService;
import com.fu.pha.service.SaleOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("api/sale")
@Validated
public class SaleController {

    @Autowired
    private SaleOrderService saleOrderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private SaleOrderRepository saleOrderRepository;

    @Autowired
    private InvoiceService invoiceService;


    @PostMapping("/create-sale-order")
    public ResponseEntity<Integer> createSaleOrder(@Valid @RequestBody SaleOrderRequestDto saleOrderRequestDto) {
        return ResponseEntity.ok(saleOrderService.createSaleOrder(saleOrderRequestDto));
    }

    @PutMapping("/update-sale-order")
    public ResponseEntity<String> updateSaleOrder(@Valid @RequestParam Long saleOrderId, @Valid @RequestBody SaleOrderRequestDto saleOrderRequestDto) {
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
            @RequestParam(defaultValue = "", name = "invoiceNumber") String invoiceNumber,
            @RequestParam(required = false, name = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false, name = "toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        Instant fromDateStart = fromDate != null ? fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant toDateEnd = toDate != null ? toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant() : null;

        Page<SaleOrderResponseDto> saleOrderResponseDto = saleOrderService.getAllSaleOrderPaging(page, size, orderType, paymentMethod, invoiceNumber, fromDateStart, toDateEnd);
        PageResponseModel<SaleOrderResponseDto> response = PageResponseModel.<SaleOrderResponseDto>builder()
                .page(page)
                .size(size)
                .total(saleOrderResponseDto.getTotalElements())
                .listData(saleOrderResponseDto.getContent())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-customer-by-customer-name")
    public ResponseEntity<List<CustomerDTOResponse>> getCustomerByCustomerName(@RequestParam String customerName) {
        return ResponseEntity.ok(customerService.getCustomerByCustomerName(customerName));
    }

    @GetMapping("/get-doctor-by-doctor-name")
    public ResponseEntity<List<DoctorDTOResponse>> getDoctorByDoctorName(@RequestParam String doctorName) {
        return ResponseEntity.ok(doctorService.getDoctorByDoctorName(doctorName));
    }

    @GetMapping("/pdf/{saleOrderId}")
    public ResponseEntity<String> generateAndDownloadInvoicePdf(
            @PathVariable Long saleOrderId,
            @RequestParam String paperSize) {

        // Tìm SaleOrder theo ID
        SaleOrder saleOrder = saleOrderRepository.findById(saleOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_NOT_FOUND));

        // Tạo file PDF và upload lên Cloudinary, trả về URL
        String pdfUrl = invoiceService.generateInvoicePdf(saleOrder, paperSize);

        if (pdfUrl != null) {
            return new ResponseEntity<>(pdfUrl, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Lỗi khi tải file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
