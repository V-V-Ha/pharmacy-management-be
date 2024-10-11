package com.fu.pha.controller;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.dto.response.PageResponseModel;
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
    public ResponseEntity<String> createSupplier(@Valid @RequestBody SupplierDto request) {
        supplierService.createSupplier(request);
        return ResponseEntity.ok(Message.CREATE_SUCCESS);
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
    public ResponseEntity<SupplierDto> getSupplierById(@RequestBody Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    // get all supplier by paging
    @GetMapping("/get-all-supplier-by-paging")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<PageResponseModel<SupplierDto>> getAllSupplierPaging(@RequestBody int page, @RequestBody int size, @RequestBody String name) {
        Page<SupplierDto> content = supplierService.getAllSupplierAndPaging(page, size, name);
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
}
