package com.fu.pha.dto.request.Payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import vn.payos.type.ItemData;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class CreatePaymentLinkRequestBody {
    private int totalAmount;
    private String description;
    private String returnUrl;
    private String cancelUrl;

}