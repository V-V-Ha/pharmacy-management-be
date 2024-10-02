package com.fu.pha.controller;

import com.fu.pha.dto.response.SampleResponse;
import com.fu.pha.service.impl.FileS3ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private FileS3ServiceImpl fileS3Service;

    @GetMapping("/all")
    public String allAccess() {
        return "Public Content.";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE') or hasRole('STOCK')")
    public String userAccess() {
        return "User Content.";
    }

    @GetMapping("/product_owner")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public String productOwnerAccess() {
        return "Product Owner Board.";
    }

    @GetMapping("/sale")
    @PreAuthorize("hasRole('SALE')")
    public String saleAccess() {
        return "Sale Board.";
    }

    @GetMapping("/stock")
    @PreAuthorize("hasRole('STOCK')")
    public String stockAccess() {
        return "Stock Board.";
    }

    @GetMapping("/sample-api/{id}")
    @ResponseBody
    public ResponseEntity<SampleResponse> sampleApi(@PathVariable int id) {
        return ResponseEntity.ok(fileS3Service.getSampleResponseById(id));
    }
}
