package com.fu.pha.dto.response.importPack;

import com.fu.pha.entity.Import;
import com.fu.pha.entity.ImportItem;
import com.fu.pha.enums.PaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
@Data
@NoArgsConstructor
public class ImportResponseDto {
    private Long id;
    private String invoiceNumber;
    private Instant importDate;
    private PaymentMethod paymentMethod;
    private Double tax;
    private Double discount;
    private Double totalAmount;
    private String note;
    private List<ImportItemResponseDto> importItems;
    private Long userId;
    private Long supplierId;
    private String supplierName;
    private String status;
    private String imageUrl;
    private Instant createDate;
    private Instant lastModifiedDate;
    private String createBy;
    private String lastModifiedBy;

    private Long productCount;

    public ImportResponseDto (Import importRequest) {
        this.id = importRequest.getId();
        this.invoiceNumber = importRequest.getInvoiceNumber();
        this.importDate = importRequest.getImportDate();
        this.paymentMethod = importRequest.getPaymentMethod();
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
        this.status = importRequest.getStatus().getValue();
        this.imageUrl = importRequest.getImage();
        this.importItems = importRequest.getImportItems().stream()
                .map(ImportItemResponseDto::new)
                .collect(Collectors.toList());
        this.productCount = importRequest.getImportItems().stream()
                .map(ImportItem::getProduct)
                .distinct()
                .count();
    }


}
