package com.fu.pha.dto.response.exportSlip;

import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.dto.response.importPack.ImportItemResponseDto;
import com.fu.pha.dto.response.importPack.ImportItemResponseForExport;
import com.fu.pha.entity.ExportSlipItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter
@NoArgsConstructor
public class ExportSlipItemResponseDto {
    private Long id;                 // ID của ExportSlipItem
    private Integer quantity;        // Số lượng sản phẩm
    private Double unitPrice;        // Đơn giá
    private String unit;             // Đơn vị tính
    private Double discount;         // Chiết khấu
    private String batchNumber;      // Số lô
    private Instant expiryDate;      // Ngày hết hạn
    private ProductDTOResponse product;          // ID của sản phẩm
    private Long exportSlipId;       // ID của phiếu xuất kho
    private Double totalAmount;      // Tổng số tiền
    private ImportItemResponseForExport importItem;       // ID của lô hàng nhập

    public ExportSlipItemResponseDto(ExportSlipItem exportSlipItem) {
        this.id = exportSlipItem.getId();
        this.quantity = exportSlipItem.getQuantity();
        this.unitPrice = exportSlipItem.getUnitPrice();
        this.unit = exportSlipItem.getUnit();
        this.discount = exportSlipItem.getDiscount();
        this.batchNumber = exportSlipItem.getBatch_number();
        this.expiryDate = exportSlipItem.getExpiryDate();
        this.product = new ProductDTOResponse(exportSlipItem.getProduct());
        this.exportSlipId = exportSlipItem.getExportSlip().getId();
        this.totalAmount = exportSlipItem.getTotalAmount();
        this.importItem = new ImportItemResponseForExport(exportSlipItem.getImportItem().getId(),
                exportSlipItem.getImportItem().getQuantity(),
                exportSlipItem.getImportItem().getUnitPrice(),
                exportSlipItem.getImportItem().getUnit(),
                exportSlipItem.getImportItem().getDiscount(),
                exportSlipItem.getImportItem().getTax(),
                exportSlipItem.getImportItem().getTotalAmount(),
                exportSlipItem.getImportItem().getBatchNumber(),
                exportSlipItem.getImportItem().getExpiryDate(),
                exportSlipItem.getImportItem().getCreateDate(),
                exportSlipItem.getImportItem().getRemainingQuantity());
    }

}
