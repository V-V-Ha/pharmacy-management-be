package com.fu.pha.dto.request.importPack;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fu.pha.entity.Import;
import com.fu.pha.entity.ImportItem;
import java.util.stream.Collectors;

import com.fu.pha.enums.OrderStatus;
import com.fu.pha.enums.PaymentMethod;

import com.fu.pha.exception.Message;
import com.fu.pha.validate.anotation.ValidDiscount;
import com.fu.pha.validate.anotation.ValidTotalAmount;
import com.fu.pha.validate.anotation.ValidVat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

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

    @ValidVat
    private Double tax;

    @ValidDiscount
    private Double discount;

    @ValidTotalAmount
    private Double totalAmount;

    private String note;

    @NotNull(message = Message.USER_NOT_NULL)
    private Long userId;

    @NotNull(message = Message.SUPPLIER_NOT_NULL)
    private Long supplierId;
}

