package com.fu.pha.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


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

    private int currentInventoryQuantity;
    private double currentInventoryAmount;

    private int outOfStockProducts;
    private int nearlyOutOfStockProducts;
    private int expiredItems;
    private int nearlyExpiredItems;


}
