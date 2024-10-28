package com.fu.pha.controller;


import com.fu.pha.dto.request.exportSlip.ExportSlipRequestDto;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.dto.response.exportSlip.ExportSlipResponseDto;
import com.fu.pha.enums.ExportType;
import com.fu.pha.exception.Message;
import com.fu.pha.service.ExportSlipService;
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

@RestController
@RequestMapping("api/export-slip")
public class ExportSlipController {

    @Autowired
    private ExportSlipService exportSlipService;

    @Autowired
    ImportService importService;

    @PostMapping("/create-export-slip")
    public ResponseEntity<String> createExportSlip(@Valid @RequestBody ExportSlipRequestDto exportSlipRequestDto) {
        exportSlipService.createExport(exportSlipRequestDto);
        return ResponseEntity.ok(Message.CREATE_SUCCESS);
    }

    @PutMapping("/update-export-slip/{exportSlipId}")
    public ResponseEntity<String> updateExportSlip(@Valid @PathVariable Long exportSlipId, @RequestBody ExportSlipRequestDto exportSlipRequestDto) {
        exportSlipService.updateExport(exportSlipId, exportSlipRequestDto);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    @DeleteMapping("/delete-export-slip/{exportSlipId}")
    public ResponseEntity<String> deleteExportSlip(@PathVariable Long exportSlipId) {
        exportSlipService.softDeleteExportSlip(exportSlipId);
        return ResponseEntity.ok(Message.DELETE_SUCCESS);
    }

    @GetMapping("/get-export-slip/{exportSlipId}")
    public ResponseEntity<?> getExportSlip(@PathVariable Long exportSlipId) {
        return ResponseEntity.ok(exportSlipService.getActiveExportSlipById(exportSlipId));
    }

    @GetMapping("/get-import-item-by-product-name")
    public ResponseEntity<?> getImportItemByProductName(@RequestParam String productName) {
        return ResponseEntity.ok(importService.getImportItemByProductName(productName));
    }

    @GetMapping("/get-all-export-slip-paging")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<PageResponseModel<ExportSlipResponseDto>> getAllExportSlipPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "", name = "exportType") ExportType exportType,
            @RequestParam(required = false, name = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false, name = "toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        Instant fromDateStart = fromDate != null ? fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant toDateEnd = toDate != null ? toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant() : null;

        Page<ExportSlipResponseDto> exportSlipResponseDtoPage = exportSlipService.getAllExportSlipPaging(page, size, exportType, fromDateStart, toDateEnd);

        PageResponseModel<ExportSlipResponseDto> response = PageResponseModel.<ExportSlipResponseDto>builder()
                .page(page)
                .size(size)
                .total(exportSlipResponseDtoPage.getTotalElements())
                .listData(exportSlipResponseDtoPage.getContent())
                .build();

        return ResponseEntity.ok(response);
    }

}
