package com.fu.pha.service;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.dto.request.importPack.ImportViewListDto;
import com.fu.pha.dto.response.ProductDtoResponseForExport;
import com.fu.pha.dto.response.importPack.ImportItemResponseDto;
import com.fu.pha.dto.response.importPack.ImportItemResponseForExport;
import com.fu.pha.dto.response.importPack.ImportResponseDto;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

public interface ImportService {

//    List<ImportItemResponseDto> addItemToImport(ImportItemResponseDto importItemDto);
//    List<ImportItemResponseDto> updateItemInImport(ImportItemResponseDto importItemDto);
//
//    void removeItemFromImport(Long productId);
//
//    List<ImportItemResponseDto> getTemporaryImportItems();

    List<UnitDto> getUnitByProductId(Long productId);

    List<ProductDTOResponse> getProductByProductName(String productName);

    void createImport(ImportDto importRequestDto, MultipartFile file);

    void updateImport(Long importId, ImportDto importDto, MultipartFile file);

    void confirmImport(Long importId, Long userId);

    void rejectImport(Long importId, String reason);

    Page<ImportViewListDto> getAllImportPaging(int page, int size, String supplierName, OrderStatus status, Instant fromDate, Instant toDate);


    ImportResponseDto getImportById(Long importId);

    List<SupplierDto> getSuppplierBySupplierName(String supplierName);
//    List<ImportItemResponseForExport> getImportItemByProductName(String productName);

    List<ProductDtoResponseForExport> getProductImportByProductName(String productName);

}
