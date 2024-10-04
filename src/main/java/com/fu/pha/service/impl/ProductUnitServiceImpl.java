package com.fu.pha.service.impl;

import com.fu.pha.entity.Product;
import com.fu.pha.entity.ProductUnit;
import com.fu.pha.entity.Unit;
import com.fu.pha.repository.ProductUnitRepository;
import com.fu.pha.service.ProductUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductUnitServiceImpl implements ProductUnitService {

    @Autowired
    ProductUnitRepository productUnitRepository;

    @Override
    public void createProductUnit(Long productId, Long unitId, Integer conversionFactor) {

    }
}
