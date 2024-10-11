package com.fu.pha.service;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.response.ProductDTOResponse;

import java.util.List;

public interface ImportService {
    List<UnitDto> getUnitByProductId(Long productId);

    ProductDTOResponse getProductByProductName(String productName);
}
