package com.fu.pha.dto.response;

import lombok.Data;

import java.time.Instant;

@Data
public class ImportItemResponseDto {
    private Long id;
    private Integer quantity;
    private Double unitPrice;
    private Long unitId;
    private Double discount;
    private Double tax;
    private Double totalAmount;
    private String batchNumber;
    private Long productId;
    private Long importId;
    private Instant createDate;
    private Instant lastModifiedDate;
    private Instant expiryDate;
    private String createBy;
    private String lastModifiedBy;
}
