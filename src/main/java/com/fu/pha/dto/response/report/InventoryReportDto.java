package com.fu.pha.dto.response.report;

import com.fu.pha.dto.response.report.reportEntity.ImportItemReportDto;
import com.fu.pha.dto.response.report.reportEntity.ProductReportDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReportDto {

    private int beginningInventoryQuantity;
    private double beginningInventoryAmount;

    private int goodsReceivedQuantity;
    private double goodsReceivedAmount;

    private int goodsIssuedQuantity;
    private double goodsIssuedAmount;

    private int goodsDestroyedQuantity;
    private double goodsDestroyedAmount;

    private int goodsReturnedQuantity;
    private double goodsReturnedAmount;

    private List<ProductReportDto> outOfStockProducts;
    private List<ProductReportDto> nearlyOutOfStockProducts;
    private List<ImportItemReportDto> expiredItems;
    private List<ImportItemReportDto> nearlyExpiredItems;

    private int currentInventoryQuantity;
    private double currentInventoryAmount;
}
