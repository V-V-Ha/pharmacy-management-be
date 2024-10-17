package com.fu.pha.service;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.request.ProductUnitDTORequest;
import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.dto.request.importPack.ImportViewListDto;
import com.fu.pha.dto.response.ImportItemResponseDto;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.ImportItem;

import java.util.List;

public interface ImportService {

    List<ImportItemResponseDto> addItemToImport(ImportItemResponseDto importItemDto);
    List<ImportItemResponseDto> updateItemInImport(ImportItemResponseDto importItemDto);

    void removeItemFromImport(Long productId);

    List<ImportItemResponseDto> getTemporaryImportItems();

    List<UnitDto> getUnitByProductId(Long productId);

    List<ProductDTOResponse> getProductByProductName(String productName);

    List<ImportViewListDto> getAllImportAndPaging();

    void createImport(ImportDto importDto);

}
