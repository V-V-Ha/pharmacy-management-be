package com.fu.pha.dto.response;

import com.fu.pha.dto.response.importPack.ImportItemResponseForExport;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ProductDtoResponseForExport {
    private String productCode;
    private String productName;
    private List<ProductUnitDTOResponse> productUnits;
    private String registrationNumber;
    private String manufacturer;
    private String countryOfOrigin;
    private List<ImportItemResponseForExport> importItems;
    private Integer totalQuantity;

    public ProductDtoResponseForExport(String productCode,String productName, List<ProductUnitDTOResponse> productUnits, String registrationNumber,
                                       String manufacturer, String countryOfOrigin, List<ImportItemResponseForExport> importItems , Integer totalQuantity) {
        this.productCode = productCode;
        this.productName = productName;
        this.productUnits = productUnits;
        this.registrationNumber = registrationNumber;
        this.manufacturer = manufacturer;
        this.countryOfOrigin = countryOfOrigin;
        this.importItems = importItems;
        this.totalQuantity = totalQuantity;
    }


}
