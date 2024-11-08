//package com.fu.pha.service.impl;
//
//import com.fu.pha.entity.ImportItem;
//import com.fu.pha.repository.ImportItemRepository;
//import com.fu.pha.service.NotificationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//
//@Service
//public class NotificationServiceImpl implements NotificationService {
//
//
//    @Autowired
//    private ImportItemRepository importItemRepository;
//
//    @Value("${expiry.warning.days}")
//    private int EXPIRY_WARNING_DAYS;
//
//    @Scheduled(cron = "0 0 8 * * ?")  // Chạy vào 8 giờ sáng mỗi ngày
//    public void checkAndNotifyExpiredProductsDaily() {
//        List<ImportItem> expiringItems = getExpiringProducts();
//
//        if (expiringItems.isEmpty()) {
//            return; // Không có sản phẩm hết hạn hoặc sắp hết hạn, thoát khỏi phương thức
//        }
//
//        for (ImportItem importItem : expiringItems) {
//            Instant expiryDate = importItem.getExpiryDate();
//            String productName = importItem.getProduct().getProductName();
//
//            if (expiryDate.isBefore(Instant.now())) {
//                // Gửi thông báo sản phẩm đã hết hạn
//                sendNotification(
//                        "Sản phẩm hết hạn",
//                        "Sản phẩm " + productName + " đã hết hạn vào ngày " + expiryDate
//                );
//            } else {
//                // Gửi thông báo sản phẩm sắp hết hạn
//                sendNotification(
//                        "Sản phẩm sắp hết hạn",
//                        "Sản phẩm " + productName + " sẽ hết hạn vào ngày " + expiryDate
//                );
//            }
//        }
//    }
//
//    private List<ImportItem> getExpiringProducts() {
//        Instant now = Instant.now();
//        Instant warningThreshold = now.plus(EXPIRY_WARNING_DAYS, ChronoUnit.DAYS);
//        return importItemRepository.findExpiringProducts(warningThreshold);
//    }
//
//    public void sendNotification(String title, String message) {
//        // Giả lập logic gửi thông báo (email, SMS, thông báo hệ thống, v.v.)
//        System.out.println("Thông báo: " + title);
//        System.out.println("Nội dung: " + message);
//    }
//}
