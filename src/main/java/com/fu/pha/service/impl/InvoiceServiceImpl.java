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

            Document document = new Document(pageSize, 10, 10, 10, 10); // Thêm lề
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // Thêm logo nếu có
            try {
                Image logo = Image.getInstance("src/main/resources/logo.png"); // Đường dẫn tới logo
                logo.scaleToFit(100, 100);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                // Nếu không có logo, tiếp tục mà không ngắt
                System.out.println("Không tìm thấy logo. Tiếp tục tạo hóa đơn mà không có logo.");
            }

            // Load font Times New Roman hỗ trợ Unicode
            BaseFont unicodeFont = BaseFont.createFont("src/main/resources/times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(unicodeFont, 8);
            Font boldFont = new Font(unicodeFont, 10, Font.BOLD);
            Font titleFont = new Font(unicodeFont, 14, Font.BOLD);
            Font smallFont = new Font(unicodeFont, 6, Font.NORMAL);

            DecimalFormat df = new DecimalFormat("#,###");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            LocalDateTime saleDateTime = saleOrder.getSaleDate().atZone(ZoneId.systemDefault()).toLocalDateTime();

            // Thông tin cửa hàng
            Paragraph storeInfo = new Paragraph("Quầy Thuốc Lê Thị Hằng\nHoằng Thành - Hoằng Hóa - Thanh Hóa\nSDT: 0379880488\n\n", boldFont);
            storeInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(storeInfo);

            // Tiêu đề hóa đơn
            Paragraph title = new Paragraph("HÓA ĐƠN BÁN LẺ", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(Chunk.NEWLINE);

            // Thông tin hóa đơn
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10f);
            infoTable.setSpacingAfter(10f);
            infoTable.setWidths(new float[]{1f, 2f});

            // Ngày và số hóa đơn
            PdfPCell cell = new PdfPCell(new Phrase("Ngày:", boldFont));
            cell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(cell);

            cell = new PdfPCell(new Phrase(dtf.format(saleDateTime), font));
            cell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(cell);

            cell = new PdfPCell(new Phrase("Số:", boldFont));
            cell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(cell);

            cell = new PdfPCell(new Phrase(saleOrder.getInvoiceNumber(), font));
            cell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(cell);

            // Khách hàng
            cell = new PdfPCell(new Phrase("Khách hàng:", boldFont));
            cell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(cell);

            cell = new PdfPCell(new Phrase(saleOrder.getCustomer().getCustomerName(), font));
            cell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(cell);

            // Số điện thoại
            cell = new PdfPCell(new Phrase("Số điện thoại:", boldFont));
            cell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(cell);

            cell = new PdfPCell(new Phrase(saleOrder.getCustomer().getPhoneNumber(), font));
            cell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(cell);

            // Nhân viên bán hàng
            cell = new PdfPCell(new Phrase("NV Bán hàng:", boldFont));
            cell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(cell);

            cell = new PdfPCell(new Phrase(saleOrder.getUser().getFullName(), font));
            cell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(cell);

            document.add(infoTable);

            // Bảng sản phẩm
            PdfPTable table = new PdfPTable(7); // 7 cột: STT, Tên hàng, ĐV, SL, CK, Đơn Giá, Thành Tiền
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[]{0.5f, 3f, 1.5f, 1f, 1.5f, 2f, 2f});  // Điều chỉnh kích thước các cột

            // Thiết lập màu nền cho tiêu đề bảng
            BaseColor headerColor = new BaseColor(224, 224, 224);

            // Thêm tiêu đề cột
            addTableHeader(table, "STT", boldFont, headerColor);
            addTableHeader(table, "Tên hàng", boldFont, headerColor);
            addTableHeader(table, "ĐV", boldFont, headerColor);
            addTableHeader(table, "SL", boldFont, headerColor);
            addTableHeader(table, "CK (%)", boldFont, headerColor); // Thêm cột CK
            addTableHeader(table, "Đơn Giá", boldFont, headerColor);
            addTableHeader(table, "Thành Tiền", boldFont, headerColor);

            // Thêm dữ liệu sản phẩm
            int index = 1;
            for (SaleOrderItem item : saleOrder.getSaleOrderItemList()) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(index++), font)));
                table.addCell(new PdfPCell(new Phrase(item.getProduct().getProductName(), font)));
                table.addCell(new PdfPCell(new Phrase(item.getUnit(), font)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), font)));
                // Thêm chiết khấu (CK)
                table.addCell(new PdfPCell(new Phrase(df.format(item.getDiscount()), font)));
                table.addCell(new PdfPCell(new Phrase(df.format(item.getUnitPrice()), font)));
                table.addCell(new PdfPCell(new Phrase(df.format(item.getTotalAmount()), font)));
            }

            // Thêm hàng tổng cộng
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("Tổng tiền", boldFont));
            totalLabelCell.setColspan(6); // Hợp nhất qua 6 cột (STT, Tên hàng, ĐV, SL, CK, Đơn Giá)
            totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabelCell.setBorder(Rectangle.TOP);
            table.addCell(totalLabelCell);

            PdfPCell totalValueCell = new PdfPCell(new Phrase(df.format(saleOrder.getTotalAmount()) + " VND", boldFont));
            totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValueCell.setBorder(Rectangle.TOP);
            table.addCell(totalValueCell);

            document.add(table);

            // Lời cảm ơn
            Paragraph thankYou = new Paragraph("Cảm ơn quý khách đã mua hàng!", boldFont);
            thankYou.setAlignment(Element.ALIGN_CENTER);
            document.add(thankYou);

            // Thêm footer với thông tin công ty
            Paragraph footer = new Paragraph("Quầy Thuốc Lê Thị Hằng | Hoằng Thành - Hoằng Hóa - Thanh Hóa | SDT: 0379880488", smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20f);
            document.add(footer);

            document.close();

            // Chuyển ByteArrayOutputStream thành byte array
            byte[] pdfBytes = baos.toByteArray();
            MultipartFile pdfMultipartFile = new CustomMultipartFile(pdfBytes, "invoice.pdf", "application/pdf");
            CloudinaryResponse response = cloudinaryService.upLoadFile(pdfMultipartFile, "invoice_" + saleOrder.getInvoiceNumber(), "raw");

            return response.getUrl();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method to add table headers with specific styles.
     *
     * @param table           The PdfPTable to add the header to.
     * @param headerTitle     The title of the header.
     * @param font            The font to use.
     * @param backgroundColor The background color of the header.
     */
    private void addTableHeader(PdfPTable table, String headerTitle, Font font, BaseColor backgroundColor) {
        PdfPCell header = new PdfPCell(new Phrase(headerTitle, font));
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.setBackgroundColor(backgroundColor);
        header.setPadding(5f);
        table.addCell(header);
    }
}
