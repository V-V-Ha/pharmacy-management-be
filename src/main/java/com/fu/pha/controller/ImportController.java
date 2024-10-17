package com.fu.pha.controller;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.request.ProductUnitDTORequest;
import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.dto.response.ImportItemResponseDto;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.ImportItem;

import com.fu.pha.exception.Message;
import com.fu.pha.service.ImportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> createImportReceipt(@RequestBody ImportDto importReceiptDto) {
        importService.createImport(importReceiptDto);
        return ResponseEntity.ok(Message.CREATE_SUCCESS);
    }

    @PostMapping("/create-item")
    public ResponseEntity<List<ImportItemResponseDto>> addItemToImport(@Valid @RequestBody ImportItemResponseDto importItemDto) {
        return ResponseEntity.ok(importService.addItemToImport(importItemDto));
    }

    @PutMapping("/update-item")
    public ResponseEntity<List<ImportItemResponseDto>> updateItemInImport(@Valid @RequestBody ImportItemResponseDto importItemDto) {
        return ResponseEntity.ok(importService.updateItemInImport(importItemDto));
    }

    @DeleteMapping("/remove-item")
    public ResponseEntity<String> removeItemFromImport(@RequestParam Long productId) {
        importService.removeItemFromImport(productId);
        return ResponseEntity.ok(Message.DELETE_SUCCESS);
    }

    @GetMapping("/get-import-items")
    public ResponseEntity<List<ImportItemResponseDto>> getTemporaryImportItems() {
        return ResponseEntity.ok(importService.getTemporaryImportItems());

    }


}
