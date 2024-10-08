package com.fu.pha.controller;

import com.fu.pha.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/import")
public class ImportController {

    @Autowired
    ImportService importService;
}
