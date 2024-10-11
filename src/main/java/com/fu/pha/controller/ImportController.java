package com.fu.pha.controller;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ProductDTOResponse> getProductByProductName(@RequestParam String productName) {
        return ResponseEntity.ok(importService.getProductByProductName(productName));
    }


}
