package com.fu.pha.dto.response.report.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryProductReportDto {
    private String productName; // Tên sản phẩm
    private int beginningInventoryQuantity; // Tồn đầu kỳ (số lượng)
    private double beginningInventoryAmount; // Tồn đầu kỳ (tổng tiền)
    private int goodsReceivedQuantity; // Nhập kho trong kỳ (số lượng)
    private double goodsReceivedAmount; // Nhập kho trong kỳ (tổng tiền)
    private int goodsIssuedQuantity; // Xuất kho trong kỳ (số lượng)
    private double goodsIssuedAmount; // Xuất kho trong kỳ (tổng tiền)
    private int endingInventoryQuantity; // Tồn cuối kỳ (số lượng)
    private double endingInventoryAmount; // Tồn cuối kỳ (tổng tiền)
}
