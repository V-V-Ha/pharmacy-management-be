package com.fu.pha.dto.response.exportSlip;

import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.dto.response.importPack.ImportItemResponseDto;
import com.fu.pha.dto.response.importPack.ImportItemResponseForExport;
import com.fu.pha.dto.response.importPack.ImportResponseDto;
import com.fu.pha.entity.ExportSlipItem;
import com.fu.pha.entity.ImportItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.stream.Collectors;

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
    private ProductDTOResponse product;          // ID của sản phẩm
    private Long exportSlipId;       // ID của phiếu xuất kho
    private Double totalAmount;      // Tổng số tiền
    private ImportItemResponseForExport importItem;
    private List<ImportItemResponseForExport> importItems;

    public ExportSlipItemResponseDto(ExportSlipItem exportSlipItem) {
        this.id = exportSlipItem.getId();
        this.quantity = exportSlipItem.getQuantity();
        this.unitPrice = exportSlipItem.getUnitPrice();
        this.unit = exportSlipItem.getUnit();
        this.discount = exportSlipItem.getDiscount();
        this.batchNumber = exportSlipItem.getBatch_number();
        this.product = new ProductDTOResponse(exportSlipItem.getProduct());
        this.exportSlipId = exportSlipItem.getExportSlip().getId();
        this.totalAmount = exportSlipItem.getTotalAmount();
        this.importItem = new ImportItemResponseForExport(exportSlipItem.getImportItem().getId(),
                exportSlipItem.getImportItem().getImportReceipt().getInvoiceNumber(),
                exportSlipItem.getImportItem().getQuantity(),
                exportSlipItem.getImportItem().getUnitPrice(),
                exportSlipItem.getImportItem().getUnit(),
                exportSlipItem.getConversionFactor(),
                exportSlipItem.getImportItem().getDiscount(),
                exportSlipItem.getImportItem().getTax(),
                exportSlipItem.getImportItem().getTotalAmount(),
                exportSlipItem.getImportItem().getBatchNumber(),
                exportSlipItem.getImportItem().getExpiryDate(),
                exportSlipItem.getImportItem().getCreateDate(),
                exportSlipItem.getImportItem().getRemainingQuantity());

        if(exportSlipItem.getProduct() != null && exportSlipItem.getProduct().getImportItems() != null){
            this.importItems = exportSlipItem.getProduct().getImportItems().stream()
                    .map(importItem -> new ImportItemResponseForExport(
                            importItem.getId(),
                            importItem.getImportReceipt().getInvoiceNumber(),
                            importItem.getQuantity(),
                            importItem.getUnitPrice(),
                            importItem.getUnit(),
                            importItem.getDiscount(),
                            importItem.getTax(),
                            importItem.getTotalAmount(),
                            importItem.getBatchNumber(),
                            importItem.getExpiryDate(),
                            importItem.getCreateDate(),
                            importItem.getRemainingQuantity()
                    ))
                    .collect(Collectors.toList());
        }
    }

}
