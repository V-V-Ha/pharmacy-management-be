package com.fu.pha.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping("/all")
    public String allAccess() {
        return "Public Content.";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('ROLE_PRODUCT_OWNER') or hasRole('ROLE_SALE') or hasRole('ROLE_STOCK')")
    public String userAccess() {
        return "User Content.";
    }

    @GetMapping("/product_owner")
    @PreAuthorize("hasRole('ROLE_PRODUCT_OWNER')")
    public String productOwnerAccess() {
        return "Product Owner Board.";
    }

    @GetMapping("/sale")
    @PreAuthorize("hasRole('ROLE_SALE')")
    public String saleAccess() {
        return "Sale Board.";
    }

    @GetMapping("/stock")
    @PreAuthorize("hasRole('ROLE_STOCK')")
    public String stockAccess() {
        return "Stock Board.";
    }
}
