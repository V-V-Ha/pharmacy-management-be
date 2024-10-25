package com.fu.pha.dto.response.exportSlip;

import com.fu.pha.dto.request.exportSlip.ExportSlipItemRequestDto;
import com.fu.pha.entity.ExportSlip;
import com.fu.pha.enums.ExportType;
import com.fu.pha.entity.ExportSlipItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class ExportSlipResponseDto {
    private Long id;
    private String invoiceNumber;
    private Instant exportDate;
    private ExportType typeDelivery;
    private Double discount;
    private Double totalAmount;
    private String note;
    private Long userId;
    private Long supplierId;
    private List<ExportSlipItemRequestDto> exportSlipItems;
    private Long productCount;

    // Constructor đúng để chuyển từ ExportSlip entity sang ExportSlipResponseDto
    public ExportSlipResponseDto(ExportSlip exportSlip) {
        this.id = exportSlip.getId();
        this.invoiceNumber = exportSlip.getInvoiceNumber();
        this.exportDate = exportSlip.getExportDate();
        this.typeDelivery = exportSlip.getTypeDelivery();
        this.discount = exportSlip.getDiscount();
        this.totalAmount = exportSlip.getTotalAmount();
        this.note = exportSlip.getNote();
        this.userId = exportSlip.getUser().getId();
        this.supplierId = exportSlip.getSupplier() != null ? exportSlip.getSupplier().getId() : null;
        this.exportSlipItems = exportSlip.getExportSlipItemList().stream()
                .map(ExportSlipItemRequestDto::new)
                .collect(Collectors.toList());
        this.productCount = exportSlip.getExportSlipItemList().stream()
                .map(ExportSlipItem::getProduct)
                .distinct()
                .count();
    }
}
