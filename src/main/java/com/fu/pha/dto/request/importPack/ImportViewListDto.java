package com.fu.pha.dto.request.importPack;

import com.fu.pha.entity.Import;
import com.fu.pha.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

@Data

public class ImportViewListDto {
    private String invoiceNumber;
    private String createBy;
    private String importDate;
    private String paymentMethod;
    private Integer numberOfProduct;
    private String supplierName;
    private String totalAmount;

    public ImportViewListDto(Import importRequest) {
       this.importDate = importRequest.getImportDate().toString();
       this.invoiceNumber = importRequest.getInvoiceNumber();
       this.createBy = importRequest.getCreateBy();
       this.paymentMethod = importRequest.getPaymentMethod().name();
       this.numberOfProduct = importRequest.getImportItems().size();
       this.supplierName = importRequest.getSupplier().getSupplierName();
       this.totalAmount = importRequest.getTotalAmount().toString();
    }

}
