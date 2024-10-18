package com.fu.pha.dto.request.importPack;

import com.fu.pha.dto.response.ImportItemResponseDto;
import com.fu.pha.entity.Import;
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
    private String tax;
    private Double discount;
    @NotNull(message = "Tổng tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Tổng tiền phải lớn hơn 0")
    private Double totalAmount;
    private String note;
    @NotNull(message = "Danh sách sản phẩm nhập không được để trống")
    private List<ImportItemResponseDto> importItems;
    @NotNull(message = "Người dùng không được để trống")
    private Long userId;
    @NotNull(message = "Nhà cung cấp không được để trống")
    private Long supplierId;
    private Instant createDate;
    private Instant lastModifiedDate;
    private String createBy;
    private String lastModifiedBy;

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
        this.createDate = importRequest.getCreateDate();
        this.lastModifiedDate = importRequest.getLastModifiedDate();
        this.createBy = importRequest.getCreateBy();
        this.lastModifiedBy = importRequest.getLastModifiedBy();
        this.importItems = importRequest.getImportItems().stream()
                .map(ImportItemResponseDto::new)
                .collect(Collectors.toList());
    }

}
