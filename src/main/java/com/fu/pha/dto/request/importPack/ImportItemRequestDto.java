package com.fu.pha.dto.request.importPack;

import com.fu.pha.exception.Message;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
@Data
public class ImportItemRequestDto {
    private Long id;
    @NotNull(message = Message.NULL_FILED)
    private Long unitId;
    private Double unitPrice;
    @NotNull(message = Message.NULL_FILED)
    private String batchNumber;
    private Instant expiryDate;
    private Long productId;
    private Long importId;


}
