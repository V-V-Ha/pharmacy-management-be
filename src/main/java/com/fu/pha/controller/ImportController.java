package com.fu.pha.controller;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.dto.request.importPack.ImportViewListDto;
import com.fu.pha.dto.response.importPack.ImportResponseDto;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.dto.response.ProductDTOResponse;

import com.fu.pha.dto.response.importPack.UserIdRequest;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.Message;
import com.fu.pha.service.ImportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("api/import")
@Validated
public class ImportController {

    @Autowired
    ImportService importService;

    //get unit by product id
    @GetMapping("/get-unit")
    public ResponseEntity<List<UnitDto>> getUnitByProductId(@RequestParam Long productId) {
         return ResponseEntity.ok(importService.getUnitByProductId(productId));
    }

    //get product by product name
    @GetMapping("/get-product")
    public ResponseEntity<List<ProductDTOResponse>> getProductByProductName(@RequestParam String productName) {
        return ResponseEntity.ok(importService.getProductByProductName(productName));
    }

    // Tạo mới phiếu nhập
    @PostMapping(value = "/create-import-receipt")
    public ResponseEntity<?> createImport(
            @Valid @RequestPart("importRequestDto") ImportDto importRequestDto,
            @RequestPart("image") MultipartFile image) {

        // Gọi service để tạo phiếu nhập
        importService.createImport(importRequestDto , image);
        return ResponseEntity.ok(Message.CREATE_SUCCESS);
    }

    @PutMapping(value = "/update-import-receipt")
    public ResponseEntity<String> updateImportReceipt(@RequestParam Long importId,
                                                      @Valid @RequestPart ImportDto importReceiptDto,
                                                      @RequestPart(value = "image",required = false) MultipartFile image) {
        importService.updateImport(importId, importReceiptDto, image);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    // Xác nhận phiếu nhập
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmImport(@PathVariable Long id, @RequestBody UserIdRequest request) {
        importService.confirmImport(id, request.getUserId());
        return ResponseEntity.ok(Message.CONFIRM_SUCCESS);
    }

    // Từ chối phiếu nhập
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectImport(@PathVariable Long id, @RequestBody UserIdRequest request) {
        importService.rejectImport(id, request.getReason());
        return ResponseEntity.ok(Message.REJECT_SUCCESS);
    }

    @GetMapping("/get-import-receipt")
    public ResponseEntity<ImportResponseDto> getImportReceiptById(@RequestParam Long importId) {
        return ResponseEntity.ok(importService.getImportById(importId));
    }

    @GetMapping("/get-supplier")
    public ResponseEntity<List<SupplierDto>> getSupplierBySupplierName(@RequestParam String supplierName) {
        return ResponseEntity.ok(importService.getSuppplierBySupplierName(supplierName));
    }

    @GetMapping("/get-all-import-paging")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<PageResponseModel<ImportViewListDto>> getAllImportPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "", name = "supplierName") String supplierName,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false, name = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false, name = "toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        Instant fromDateStart = fromDate != null ? fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant toDateEnd = toDate != null ? toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant() : null;

        Page<ImportViewListDto> importDtoPage = importService.getAllImportPaging(page, size, supplierName, status, fromDateStart, toDateEnd);

        PageResponseModel<ImportViewListDto> response = PageResponseModel.<ImportViewListDto>builder()
                .page(page)
                .size(size)
                .total(importDtoPage.getTotalElements())
                .listData(importDtoPage.getContent())
                .build();

        return ResponseEntity.ok(response);
    }


}
