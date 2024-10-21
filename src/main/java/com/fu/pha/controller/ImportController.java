package com.fu.pha.controller;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.request.ProductUnitDTORequest;
import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.dto.request.importPack.ImportViewListDto;
import com.fu.pha.dto.response.ImportItemResponseDto;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.ImportItem;

import com.fu.pha.exception.Message;
import com.fu.pha.service.ImportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("api/import")
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

    @PostMapping("/create-import-receipt")
    public ResponseEntity<String> createImportReceipt(@Valid @RequestBody ImportDto importReceiptDto ) {
        importService.createImport(importReceiptDto);
        return ResponseEntity.ok(Message.CREATE_SUCCESS);
    }

    @PutMapping("/update-import-receipt")
    public ResponseEntity<String> updateImportReceipt(@RequestParam Long importId, @Valid @RequestBody ImportDto importReceiptDto) {
        importService.updateImport(importId, importReceiptDto);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    @GetMapping("/get-import-receipt")
    public ResponseEntity<ImportDto> getImportReceiptById(@RequestParam Long importId) {
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
            @RequestParam(required = false, name = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false, name = "toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        Instant fromDateStart = fromDate != null ? fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant toDateEnd = toDate != null ? toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant() : null;

        Page<ImportViewListDto> importDtoPage = importService.getAllImportPaging(page, size, supplierName, fromDateStart, toDateEnd);

        PageResponseModel<ImportViewListDto> response = PageResponseModel.<ImportViewListDto>builder()
                .page(page)
                .size(size)
                .total(importDtoPage.getTotalElements())
                .listData(importDtoPage.getContent())
                .build();

        return ResponseEntity.ok(response);
    }


}
