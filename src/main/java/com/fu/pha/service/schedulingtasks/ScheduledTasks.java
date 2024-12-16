package com.fu.pha.service.schedulingtasks;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import com.fu.pha.dto.response.report.product.ExpiredProductDto;
import com.fu.pha.dto.response.report.product.OutOfStockProductDto;
import com.fu.pha.entity.ImportItem;
import com.fu.pha.entity.Product;
import com.fu.pha.enums.NotificationType;
import com.fu.pha.repository.ImportItemRepository;
import com.fu.pha.repository.NotificationRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class ScheduledTasks {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImportItemRepository importItemRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(cron = "0 0 7 * * ?", zone = "Asia/Ho_Chi_Minh") // Hằng ngày lúc 15:20
//    @Scheduled(cron = "0 0/10 * * * ?") // Mỗi 10 phút
    public void reportCurrentTime() {
        log.info("The time is now {}", dateFormat.format(new Date()));
    }



    /**
     * Kiểm tra và thông báo sản phẩm sắp hết hàng.
     */
    @Scheduled(cron = "0 0 7 * * ?", zone = "Asia/Ho_Chi_Minh") // Hằng ngày lúc 15:20
//    @Scheduled(cron = "0 0/10 * * * ?") // Mỗi 10 phút

    public void checkOutOfStockProducts() {
        List<OutOfStockProductDto> outOfStockProducts = notificationService.getOutOfStockProducts1(null, null, 0, 100).getContent();
        if (!outOfStockProducts.isEmpty()) {
            notificationService.createOutOfStockNotifications(outOfStockProducts);
        }
    }

    /**
     * Kiểm tra và thông báo sản phẩm hết hàng.
     */
    @Scheduled(cron = "0 0 7 * * ?", zone = "Asia/Ho_Chi_Minh") // Hằng ngày lúc 15:20
//    @Scheduled(cron = "0 0/10 * * * ?") // Mỗi 10 phút

    public void checkLowStockProducts() {
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        if (!lowStockProducts.isEmpty()) {
            notificationService.createLowStockNotifications(lowStockProducts);
        }
    }

    /**
     * Kiểm tra và thông báo sản phẩm sắp hết hạn.
     */
    @Scheduled(cron = "0 0 7 * * ?", zone = "Asia/Ho_Chi_Minh") // Hằng ngày lúc 15:20
//    @Scheduled(cron = "0 0/10 * * * ?") // Mỗi 10 phút
    public void checkNearlyExpiredProducts() {
        int warningDays = 90; // Cảnh báo trước 60 ngày
        Instant fiveDaysAgo = Instant.now().minus(5, ChronoUnit.DAYS);

        List<ExpiredProductDto> expiredProducts = notificationService.getExpiredProducts(null, null, warningDays, 0, 100).getContent();

        if (!expiredProducts.isEmpty()) {
            // Lọc các sản phẩm chưa được thông báo trong 5 ngày qua
            List<ExpiredProductDto> filteredProducts = expiredProducts.stream()
                    .filter(product -> !notificationRepository.existsByBatchNumberAndTypeAndCreatedAtAfter(
                            product.getBatchNumber(), NotificationType.EXPIRED, fiveDaysAgo))
                    .toList();

            if (!filteredProducts.isEmpty()) {
                notificationService.createNearlyExpiredProductNotifications(filteredProducts);
            }
        }
    }

    /**
     * Kiểm tra và thông báo sản phẩm đã hết hạn.
     */
    @Scheduled(cron = "0 0 7 * * ?", zone = "Asia/Ho_Chi_Minh") // Hằng ngày lúc 15:20
//    @Scheduled(cron = "0 0/10 * * * ?") // Mỗi 10 phút
    public void checkExpiredProducts() {

        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate today = LocalDate.now(zoneId);
        LocalDate tomorrow = today.plusDays(1);
        ZonedDateTime zonedDateTimeTomorrow = tomorrow.atStartOfDay(zoneId);
        Instant tomorrowInstant = zonedDateTimeTomorrow.toInstant();

        List<ImportItem> expiredProducts = importItemRepository.findExpiredProducts(tomorrowInstant);
        if (!expiredProducts.isEmpty()) {
            notificationService.createExpiredProductNotifications(expiredProducts);
        }
    }
}
