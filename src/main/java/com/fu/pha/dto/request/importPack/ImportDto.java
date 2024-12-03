package com.fu.pha.dto.request.importPack;


import com.fu.pha.enums.PaymentMethod;

import com.fu.pha.exception.Message;
import com.fu.pha.validate.anotation.ValidTotalAmount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class ImportDto {
    @NotNull(message = Message.DATE_NOT_NULL)
    private Instant importDate;

    @NotNull(message = Message.LIST_ITEM_NOT_NULL)
    @Valid
    private List<ImportItemRequestDto> importItems;

    @NotNull(message = Message.PAYMENT_METHOD_NOT_NULL)
    private PaymentMethod paymentMethod;

    private Double tax;

    private Double discount;

    @ValidTotalAmount
    private Double totalAmount;

    private String note;

    private Long userId;

    @NotNull(message = Message.SUPPLIER_NOT_NULL)
    private Long supplierId;
}

