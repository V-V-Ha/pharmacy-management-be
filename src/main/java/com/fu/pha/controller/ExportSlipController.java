package com.fu.pha.controller;


import com.fu.pha.dto.request.exportSlip.ExportSlipRequestDto;
import com.fu.pha.exception.Message;
import com.fu.pha.service.ExportSlipService;
import com.fu.pha.service.ImportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
