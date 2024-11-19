package com.fu.pha.service;

import com.fu.pha.entity.SaleOrder;

public interface InvoiceService {

    String generateInvoicePdf(SaleOrder saleOrder, String paperSize);

}
