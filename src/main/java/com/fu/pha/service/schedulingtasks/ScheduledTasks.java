package com.fu.pha.service.schedulingtasks;
import java.text.SimpleDateFormat;
import java.time.Instant;
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

    @Scheduled(cron = "0 0 7 * * ?") // Hằng ngày lúc 07:00
    public void reportCurrentTime() {
        log.info("The time is now {}", dateFormat.format(new Date()));
    }



    /**
     * Kiểm tra và thông báo sản phẩm hết hàng.
     */
    @Scheduled(cron = "0 0 7 * * ?") // Hằng ngày lúc 07:00
    public void checkOutOfStockProducts() {
        List<OutOfStockProductDto> outOfStockProducts = notificationService.getOutOfStockProducts(null, null, 0, 100).getContent();
        if (!outOfStockProducts.isEmpty()) {
            notificationService.createOutOfStockNotifications(outOfStockProducts);
        }
    }

    /**
     * Kiểm tra và thông báo sản phẩm sắp hết hàng.
     */
    @Scheduled(cron = "0 0 7 * * ?") // Hằng ngày lúc 07:00
    public void checkLowStockProducts() {
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        if (!lowStockProducts.isEmpty()) {
            notificationService.createLowStockNotifications(lowStockProducts);
        }
    }

    /**
     * Kiểm tra và thông báo sản phẩm sắp hết hạn.
     */
    @Scheduled(cron = "0 0 7 * * ?") // Hằng ngày lúc 07:00
//    @Scheduled(cron = "0 0/1 * * * ?") // Mỗi phút
    public void checkNearlyExpiredProducts() {
        int warningDays = 60; // Cảnh báo trước 60 ngày
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
    @Scheduled(cron = "0 0 7 * * ?") // Hằng ngày lúc 07:00
//    @Scheduled(cron = "0 0/1 * * * ?") // Mỗi phút
    public void checkExpiredProducts() {
        List<ImportItem> expiredProducts = importItemRepository.findExpiredProducts();
        if (!expiredProducts.isEmpty()) {
            notificationService.createExpiredProductNotifications(expiredProducts);
        }
    }
}
