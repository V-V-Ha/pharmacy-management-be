package com.fu.pha.controller;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.dto.response.SupplierCResponseDto;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.Message;
import com.fu.pha.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/supplier")
public class SupplierController {

    @Autowired
    SupplierService supplierService;

    //create supplier
    @PostMapping("/create-supplier")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<SupplierCResponseDto> createSupplier(@Valid @RequestBody SupplierDto request) {
        return ResponseEntity.ok(supplierService.createSupplier(request));
    }

    //update supplier
    @PutMapping("/update-supplier")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> updateSupplier(@Valid @RequestBody SupplierDto request) {
        supplierService.updateSupplier(request);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    //get supplier by id
    @GetMapping("/get-supplier-by-id")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<SupplierDto> getSupplierById(@RequestParam Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    // get all supplier by paging
    @GetMapping("/get-all-supplier-by-paging")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<PageResponseModel<SupplierDto>> getAllSupplierPaging(@RequestParam(defaultValue = "0") int page,
                                                                               @RequestParam(defaultValue = "10") int size,
                                                                               @RequestParam(required = false) String name,
                                                                               @RequestParam(required = false) String status) {
        Page<SupplierDto> content = supplierService.getAllSupplierAndPaging(page, size, name, status);
        PageResponseModel<SupplierDto> response = PageResponseModel.<SupplierDto>builder()
                .page(page)
                .size(size)
                .total(content.getTotalElements())
                .listData(content.getContent())
                .build();
        return ResponseEntity.ok(response);
    }

    // get all supplier
    @GetMapping("/get-all-supplier")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<List<SupplierDto>> getAllSupplier() {
        return ResponseEntity.ok(supplierService.getAllSupplier());
    }

    @PutMapping("/change-status-supplier")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> updateSupplierStatus(@RequestParam Long id) {
        supplierService.updateSupplierStatus(id);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

}
