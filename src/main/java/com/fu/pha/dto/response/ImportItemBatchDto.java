package com.fu.pha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImportItemBatchDto {
    private String invoiceNumber;
    private String batchNumber;
    private Integer quantityToSell;
}
