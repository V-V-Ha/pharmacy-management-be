package com.fu.pha.service.schedulingtasks;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.fu.pha.dto.response.report.product.ExpiredProductDto;
import com.fu.pha.dto.response.report.product.OutOfStockProductDto;
import com.fu.pha.entity.ImportItem;
import com.fu.pha.entity.Product;
import com.fu.pha.repository.ImportItemRepository;
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

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(cron = "0 0 0 * * ?")
    public void reportCurrentTime() {
        log.info("The time is now {}", dateFormat.format(new Date()));
    }



    @Scheduled(cron = "0 0 0 * * ?") // Hằng ngày lúc 00:00
    public void checkOutOfStockProducts() {
        List<OutOfStockProductDto> outOfStockProducts = notificationService.getOutOfStockProducts(null, null, 0, 100).getContent();
        if (!outOfStockProducts.isEmpty()) {
            notificationService.createOutOfStockNotifications(outOfStockProducts);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // Hằng ngày lúc 00:00
    public void checkLowStockProducts() {
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        if (!lowStockProducts.isEmpty()) {
            notificationService.createLowStockNotifications(lowStockProducts);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // Hằng ngày lúc 00:00
    public void checkNearlyExpiredProducts() {
        int warningDays = 60; // Cảnh báo trước 60 ngày
        List<ExpiredProductDto> expiredProducts = notificationService.getExpiredProducts(null, null, warningDays, 0, 100).getContent();
        if (!expiredProducts.isEmpty()) {
            notificationService.createNearlyExpiredProductNotifications(expiredProducts);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // Hằng ngày lúc 00:00
    public void checkExpiredProducts() {
        List<ImportItem> expiredProducts = importItemRepository.findExpiredProducts();
        if (!expiredProducts.isEmpty()) {
            notificationService.createExpiredProductNotifications(expiredProducts);
        }
    }


//    @Scheduled(fixedRate = 5000)
//    public void checkOutOfStockProducts() {
//        log.info("Starting check for out-of-stock products...");
//        List<OutOfStockProductDto> outOfStockProducts = notificationService.getOutOfStockProducts(null, null, 0, 100).getContent();
//        if (outOfStockProducts.isEmpty()) {
//            log.info("No out-of-stock products found.");
//        } else {
//            log.info("Found {} out-of-stock products.", outOfStockProducts.size());
//            notificationService.createOutOfStockNotifications(outOfStockProducts);
//        }
//    }
//
//    @Scheduled(fixedRate = 5000)
//    public void checkExpiredProducts() {
//        log.info("Starting check for expired products...");
//        int warningDays = 7;
//        List<ExpiredProductDto> expiredProducts = notificationService.getExpiredProducts(null, null, warningDays, 0, 100).getContent();
//        if (expiredProducts.isEmpty()) {
//            log.info("No expired products found.");
//        } else {
//            log.info("Found {} expired products.", expiredProducts.size());
//            notificationService.createExpiredProductNotifications(expiredProducts);
//        }
//    }

}
