package com.fu.pha.dto.request.importPack;

import com.fu.pha.entity.Import;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class ImportDto {
    private Long id;
    private String invoiceNumber;
    private Instant importDate;
    private String paymentMethod;
    private Double taxiDentificationNumber;
    private Double discount;
    private Double totalAmount;
    private String note;
    private List<ImportItemDto> importItemList;
    private Long userId;
    private Long supplierId;
    private Instant createDate;
    private Instant lastModifiedDate;
    private String createBy;
    private String lastModifiedBy;

    public ImportDto (Import importRequest) {
        this.id = importRequest.getId();
        this.invoiceNumber = importRequest.getInvoiceNumber();
        this.importDate = importRequest.getImportDate();
        this.paymentMethod = importRequest.getPaymentMethod().name();
        this.taxiDentificationNumber = importRequest.getTaxiDentificationNumber();
        this.discount = importRequest.getDiscount();
        this.totalAmount = importRequest.getTotalAmount();
        this.note = importRequest.getNote();
        this.userId = importRequest.getUser().getId();
        this.supplierId = importRequest.getSupplier().getId();
        this.createDate = importRequest.getCreateDate();
        this.lastModifiedDate = importRequest.getLastModifiedDate();
        this.createBy = importRequest.getCreateBy();
        this.lastModifiedBy = importRequest.getLastModifiedBy();
    }

}
