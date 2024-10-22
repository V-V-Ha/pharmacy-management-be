package com.fu.pha.dto.request.exportSlip;

import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.ExportSlipItem;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter
public class ExportSlipItemRequestDto {
    private Long id;                 // ID của ExportSlipItem
    private Integer quantity;        // Số lượng sản phẩm
    private Double unitPrice;        // Đơn giá
    private String unit;             // Đơn vị tính
    private Double discount;         // Chiết khấu
    private String batchNumber;      // Số lô
    private Instant expirationDate;  // Ngày hết hạn
    private Instant productionDate;  // Ngày sản xuất
    private Long productId;          // ID của sản phẩm
    private Long exportSlipId;       // ID của phiếu xuất kho
    private Double totalAmount;      // Tổng số tiền
    private Long importItemId;       // ID của lô hàng nhập

    public ExportSlipItemRequestDto(ExportSlipItem exportSlipItem) {
        this.id = exportSlipItem.getId();
        this.quantity = exportSlipItem.getQuantity();
        this.unitPrice = exportSlipItem.getUnitPrice();
        this.unit = exportSlipItem.getUnit();
        this.discount = exportSlipItem.getDiscount();
        this.batchNumber = exportSlipItem.getBatchNumber();
        this.expirationDate = exportSlipItem.getExpirationDate();
        this.productionDate = exportSlipItem.getProductionDate();
        this.productId = exportSlipItem.getProduct().getId();
        this.exportSlipId = exportSlipItem.getExportSlip().getId();
        this.totalAmount = exportSlipItem.getTotalAmount();
        this.importItemId = exportSlipItem.getImportItem().getId();
    }
}
