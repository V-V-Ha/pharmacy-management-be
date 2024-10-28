package com.fu.pha.controller;

import com.fu.pha.service.SaleOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/sale")
public class SaleController {

    @Autowired
    private SaleOrderService saleOrderService;
}
