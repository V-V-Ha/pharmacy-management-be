package com.fu.pha.dto.request.exportSlip;

import com.fu.pha.enums.ExportType;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExportSlipDto {
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
}
