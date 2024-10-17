package com.fu.pha.dto.request.importPack;

import com.fu.pha.dto.request.ProductUnitDTORequest;
import com.fu.pha.exception.Message;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class ImportItemRequestDto {
    private Long id;
    @NotNull(message = Message.NULL_FILED)
    private Long unit;
    private Double unitPrice;
    @NotNull(message = Message.NULL_FILED)
    private String batchNumber;
    private Long productId;
    private Long importId;
    private Integer quantity;
    private Double discount;
    private String tax;
    private Instant expiryDate;
    private Double totalAmount;
}
