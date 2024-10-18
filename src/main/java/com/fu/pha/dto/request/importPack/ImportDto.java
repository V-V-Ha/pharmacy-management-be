package com.fu.pha.dto.request.importPack;

import com.fu.pha.dto.request.ProductUnitDTORequest;
import com.fu.pha.dto.response.ImportItemResponseDto;
import com.fu.pha.entity.Import;
import java.util.stream.Collectors;
import com.fu.pha.entity.ImportItem;
import com.fu.pha.entity.ProductUnit;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class ImportDto {
    private Long id;
    private String invoiceNumber;
    private Instant importDate;
    private String paymentMethod;

    private String tax;
    private Double discount;
    private Double totalAmount;
    private String note;
    private List<ImportItemResponseDto> importItems;
    private Long userId;
    private Long supplierId;
    private String supplierName;
    private Instant createDate;
    private Instant lastModifiedDate;
    private String createBy;
    private String lastModifiedBy;

    private Long productCount;

    public ImportDto (Import importRequest) {
        this.id = importRequest.getId();
        this.invoiceNumber = importRequest.getInvoiceNumber();
        this.importDate = importRequest.getImportDate();
        this.paymentMethod = importRequest.getPaymentMethod().name();
        this.tax = importRequest.getTax();
        this.discount = importRequest.getDiscount();
        this.totalAmount = importRequest.getTotalAmount();
        this.note = importRequest.getNote();
        this.userId = importRequest.getUser().getId();
        this.supplierId = importRequest.getSupplier().getId();
        this.supplierName = importRequest.getSupplier().getSupplierName();
        this.createDate = importRequest.getCreateDate();
        this.lastModifiedDate = importRequest.getLastModifiedDate();
        this.createBy = importRequest.getCreateBy();
        this.lastModifiedBy = importRequest.getLastModifiedBy();
        this.importItems = importRequest.getImportItems().stream()
                .map(ImportItemResponseDto::new)
                .collect(Collectors.toList());
        this.productCount = importRequest.getImportItems().stream()
                .map(ImportItem::getProduct)
                .distinct()
                .count();
    }

}
