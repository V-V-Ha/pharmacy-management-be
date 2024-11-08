package com.fu.pha.service;

import com.fu.pha.entity.SaleOrder;

public interface InvoiceService {

    byte[] generateInvoicePdf(SaleOrder saleOrder, String paperSize);

}
