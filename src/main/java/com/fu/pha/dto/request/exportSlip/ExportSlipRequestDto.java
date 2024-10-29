package com.fu.pha.dto.request.exportSlip;

import com.fu.pha.enums.ExportType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExportSlipRequestDto {
    private Long id;
    private String invoiceNumber;
    private Instant exportDate;
    private ExportType typeDelivery;
    private Double discount;
    private Double totalAmount;
    private String note;
    private Long userId;
    private Long supplierId;
    @NotNull(message = "Danh sách sản phẩm nhập không được để trống")
    private List<ExportSlipItemRequestDto> exportSlipItems;
    private Long productCount;
}
