package com.fu.pha.controller;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.exception.Message;
import com.fu.pha.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unit")
public class UnitController {
    @Autowired
    UnitService unitService;

    @GetMapping("/get-all-unit")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<PageResponseModel<UnitDto>> getAllUnitPaging(@RequestParam int page,
                                                                          @RequestParam int size,
                                                                       @RequestParam(required = false) String name) {
        Page<UnitDto> content = unitService.getAllUnitPaging(page, size, name);

        PageResponseModel<UnitDto> response = PageResponseModel.<UnitDto>builder()
                .page(page)
                .size(size)
                .total(content.getTotalElements())
                .listData(content.getContent())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-unit-by-id")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<UnitDto> getUnitById(@RequestParam Long id) {
        return ResponseEntity.ok(unitService.getUnitById(id));
    }

    @PostMapping("/create-unit")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> createUnit(@RequestBody UnitDto request) {
        unitService.createUnit(request);
        return ResponseEntity.ok(Message.CREATE_SUCCESS);
    }

    @PutMapping("/update-unit")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> updateUnit(@RequestBody UnitDto request) {
        unitService.updateUnit(request);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    @DeleteMapping("/delete-unit")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> deleteUnit(@RequestParam Long id) {
        unitService.deleteUnit(id);
        return ResponseEntity.ok(Message.DELETE_SUCCESS);
    }

    @GetMapping("/get-all-unit-list")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<List<UnitDto>> getAllUnit() {
        return ResponseEntity.ok(unitService.getAllUnit());
    }
}
