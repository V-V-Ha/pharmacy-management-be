package com.fu.pha.dto.request.importPack;

import com.fu.pha.exception.Message;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
@Data
public class ImportItemDto {
    private Long id;
    @NotNull(message = Message.NULL_FILED)
    private Integer quantity;
    private Double unitPrice;
    @NotNull(message = Message.NULL_FILED)
    private Long unitId;
    private Double discount;
    private Double tax;
    @NotNull(message = Message.NULL_FILED)
    private Double totalAmount;
    @NotNull(message = Message.NULL_FILED)
    private String batchNumber;
    private Instant expiryDate;
    private Long productId;
    private Long importId;
}
