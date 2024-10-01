package com.fu.pha.service;

public interface ProductUnitService {
    //create product unit
    void createProductUnit(Long productId, Long unitId, Integer conversionFactor);
}
