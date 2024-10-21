package com.fu.pha.dto.request.importPack;

import com.fu.pha.entity.Import;
import com.fu.pha.entity.ImportItem;
import com.fu.pha.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data

public class ImportViewListDto {
    private Long id;
    private String invoiceNumber;
    private String createBy;
    private Instant importDate;
    private String paymentMethod;
    private String supplierName;
    private Double totalAmount;
    private Integer productCount;
    private String fullName;

    public ImportViewListDto(Import importRequest) {
        this.id = importRequest.getId();
       this.importDate = importRequest.getImportDate();
       this.invoiceNumber = importRequest.getInvoiceNumber();
       this.createBy = importRequest.getCreateBy();
       this.paymentMethod = importRequest.getPaymentMethod().name();
        this.productCount = (int) importRequest.getImportItems().stream()
                .map(ImportItem::getProduct)
                .distinct()
                .count();
       this.supplierName = importRequest.getSupplier().getSupplierName();
       this.totalAmount = importRequest.getTotalAmount();
       this.fullName = importRequest.getUser().getFullName();
    }

}
