package com.fu.pha.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data@NoArgsConstructor
public class SupplierResponseDto {
    private Long id;
    private String supplierName;
    
    public SupplierResponseDto(Long id, String supplierName) {
        this.id = id;
        this.supplierName = supplierName;
    }


}
