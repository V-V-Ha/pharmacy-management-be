package com.fu.pha.dto.request.importPack;

import com.fu.pha.entity.Import;
import com.fu.pha.entity.ImportItem;
import java.util.stream.Collectors;
import com.fu.pha.enums.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class ImportDto {
    private Long id;
    private String invoiceNumber;
    @NotNull(message = "Ngày nhập không đươc để trống")
    private Instant importDate;
    private PaymentMethod paymentMethod;
    private Double tax;
    private Double discount;
    @NotNull(message = "Tổng tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Tổng tiền phải lớn hơn 0")
    private Double totalAmount;
    private String note;
    @NotNull(message = "Danh sách sản phẩm nhập không được để trống")
    private List<ImportItemRequestDto> importItems;
    @NotNull(message = "Người dùng không được để trống")
    private Long userId;
    @NotNull(message = "Nhà cung cấp không được để trống")
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
        this.importItems = importRequest.getImportItems().stream()
                .map(ImportItemRequestDto::new)
                .collect(Collectors.toList());
        this.productCount = importRequest.getImportItems().stream()
                .map(ImportItem::getProduct)
                .distinct()
                .count();
    }

}
