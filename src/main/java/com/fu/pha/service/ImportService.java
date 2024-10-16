package com.fu.pha.service;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.request.ProductUnitDTORequest;
import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.dto.request.importPack.ImportViewListDto;
import com.fu.pha.dto.response.ProductDTOResponse;

import java.util.List;

public interface ImportService {
    List<UnitDto> getUnitByProductId(Long productId);

    ProductDTOResponse getProductByProductName(String productName);

    List<ImportViewListDto> getAllImportAndPaging();

    void createImport(ProductUnitDTORequest productUnitDTORequest , ImportDto importDto);
}
