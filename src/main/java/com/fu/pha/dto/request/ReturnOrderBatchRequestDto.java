package com.fu.pha.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnOrderBatchRequestDto {
    private String invoiceNumber;
    private String batchNumber;
    private Integer quantity;
}
