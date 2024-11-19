package com.fu.pha.dto.response.SaleOrder;

import com.fu.pha.dto.request.SaleOrder.SaleOrderItemRequestDto;
import com.fu.pha.dto.response.CustomerDTOResponse;
import com.fu.pha.dto.response.DoctorDTOResponse;
import com.fu.pha.dto.response.importPack.ImportItemResponseDto;
import com.fu.pha.entity.SaleOrder;
import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class SaleOrderResponseDto {
    private Long id;
    private String invoiceNumber;
    private Instant saleDate;
    private OrderType orderType;
    private PaymentMethod paymentMethod;
    private Double discount;
    private Double totalAmount;
    private CustomerDTOResponse customer;
    private DoctorDTOResponse doctor;
    private Long userId;
    private String diagnosis;
    private List<SaleOrderItemResponseDto> saleOrderItems;
    private Instant lastModifiedDate;
    private Instant createdDate;
    private String lastModifiedBy;
    private String createdBy;

    public SaleOrderResponseDto(SaleOrder saleOrder) {
        this.id = saleOrder.getId();
        this.invoiceNumber = saleOrder.getInvoiceNumber();
        this.saleDate = saleOrder.getSaleDate();
        this.orderType = saleOrder.getOrderType();
        this.paymentMethod = saleOrder.getPaymentMethod();
        this.discount = saleOrder.getDiscount();
        this.totalAmount = saleOrder.getTotalAmount();
        this.customer = new CustomerDTOResponse(saleOrder.getCustomer());
        this.doctor = saleOrder.getDoctor() != null ? new DoctorDTOResponse(saleOrder.getDoctor()) : null;
        this.userId = saleOrder.getUser().getId();
        this.diagnosis = saleOrder.getDiagnosis();
        this.saleOrderItems = saleOrder.getSaleOrderItemList().stream()
                .map(SaleOrderItemResponseDto::new)
                .collect(Collectors.toList());
        this.createdBy = saleOrder.getCreateBy();
        this.lastModifiedBy = saleOrder.getLastModifiedBy();
        this.createdDate = saleOrder.getCreateDate();
        this.lastModifiedDate = saleOrder.getLastModifiedDate();
    }




}
