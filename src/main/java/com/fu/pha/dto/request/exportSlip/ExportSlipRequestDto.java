package com.fu.pha.dto.request.exportSlip;

import com.fu.pha.enums.ExportType;
import com.fu.pha.validate.anotation.ValidTotalAmount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
    @NotNull(message = "Ngày xuất không được để trống")
    private Instant exportDate;
    @NotNull(message = "Loại phiếu xuất không được để trống")
    private ExportType typeDelivery;
    private Double discount;
    @ValidTotalAmount
    private Double totalAmount;
    private String note;
    private Long userId;
    private Long supplierId;
    @NotEmpty(message = "Danh sách sản phẩm nhập không được để trống")
    @Valid
    private List<ExportSlipItemRequestDto> exportSlipItems;
    private Long productCount;
    private String status;
}
