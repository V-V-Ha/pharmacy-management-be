package com.fu.pha.dto.request.Payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CreatePaymentLinkRequestBody {
    private int totalAmount;
    private String description;
    private String returnUrl;
    private String cancelUrl;

}