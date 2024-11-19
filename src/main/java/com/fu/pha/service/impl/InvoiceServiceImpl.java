package com.fu.pha.service.impl;

import com.fu.pha.dto.response.CloudinaryResponse;
import com.fu.pha.entity.SaleOrder;
import com.fu.pha.entity.SaleOrderItem;
import com.fu.pha.service.CloudinaryService;
import com.fu.pha.service.InvoiceService;
import com.fu.pha.util.CustomMultipartFile;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.itextpdf.text.pdf.BaseFont;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private CloudinaryService cloudinaryService;
    @Override
    public String generateInvoicePdf(SaleOrder saleOrder, String paperSize) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Rectangle pageSize;
            switch (paperSize) {
                case "58mm":
                    pageSize = new Rectangle(165, 842);
                    break;
                case "80mm":
                    pageSize = new Rectangle(226, 842);
                    break;
                case "A5":
                    pageSize = PageSize.A5;
                    break;
                default:
                    pageSize = new Rectangle(165, 842);
            }

            Document document = new Document(pageSize);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Load font Times New Roman hỗ trợ Unicode
            BaseFont unicodeFont = BaseFont.createFont("src/main/resources/times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(unicodeFont, 8);
            Font boldFont = new Font(unicodeFont, 8, Font.BOLD);

            DecimalFormat df = new DecimalFormat("#,###");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            LocalDateTime saleDateTime = saleOrder.getSaleDate().atZone(ZoneId.systemDefault()).toLocalDateTime();

            // Thông tin cửa hàng
            Paragraph storeInfo = new Paragraph("Quầy Thuốc Lê Thị Hằng\nHoằng Thành - Hoằng Hóa - Thanh Hóa\nSDT: 0379880488\n\n", boldFont);
            storeInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(storeInfo);

            // Tiêu đề hóa đơn
            Paragraph title = new Paragraph("HÓA ĐƠN BÁN LẺ", boldFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Ngày: " + dtf.format(saleDateTime) + "    Số: " + saleOrder.getInvoiceNumber(), boldFont));
            document.add(new Paragraph("Khách hàng: " + saleOrder.getCustomer().getCustomerName(), boldFont));
            document.add(new Paragraph("Số điện thoại: " + saleOrder.getCustomer().getPhoneNumber(), boldFont));
            document.add(new Paragraph("NV Bán hàng: " + saleOrder.getUser().getFullName(), boldFont));

            document.add(new Paragraph("\n"));

            // Bảng sản phẩm
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);

            table.getDefaultCell().setBorderWidth(0.02f);
            table.setWidths(new float[]{ 3f, 1.3f, 1.1f, 2.5f, 2.5f});  // Điều chỉnh kích thước các cột


            // Thêm tiêu đề cột và ngăn xuống dòng

            PdfPCell cell = new PdfPCell(new Phrase("Tên hàng", boldFont));
            cell.setNoWrap(true);
            cell.setBorderWidth(0.02f);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("ĐV", boldFont));
            cell.setNoWrap(true);
            cell.setBorderWidth(0.02f);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("SL", boldFont));
            cell.setNoWrap(true);
            cell.setBorderWidth(0.02f);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Giá", boldFont));
            cell.setNoWrap(true);
            cell.setBorderWidth(0.02f);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Tiền", boldFont));
            cell.setNoWrap(true);
            cell.setBorderWidth(0.02f);
            table.addCell(cell);

            int index = 1;
            for (SaleOrderItem item : saleOrder.getSaleOrderItemList()) {
                table.addCell(new PdfPCell(new Phrase(item.getProduct().getProductName(), font)));
                table.addCell(new PdfPCell(new Phrase(item.getUnit(), font)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), font)));
                table.addCell(new PdfPCell(new Phrase(df.format(item.getUnitPrice()), font)));
                table.addCell(new PdfPCell(new Phrase(df.format(item.getTotalAmount()), font)));

            }

            document.add(table);


            document.add(new Paragraph("Tổng tiền: " + df.format(saleOrder.getTotalAmount()) + " VND", boldFont));
            document.add(new Paragraph("--------------------------------------------------", font));
            document.add(new Paragraph("\nCảm ơn quý khách đã mua hàng", boldFont));

            document.close();

            // Chuyển ByteArrayOutputStream thành byte array
            byte[] pdfBytes = baos.toByteArray();
            MultipartFile pdfMultipartFile = new CustomMultipartFile(pdfBytes, "invoice.pdf", "application/pdf");
            CloudinaryResponse response = cloudinaryService.upLoadFile(pdfMultipartFile, "invoice_" + saleOrder.getInvoiceNumber(),"raw");

            return response.getUrl();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }




}






