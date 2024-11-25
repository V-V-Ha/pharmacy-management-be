package com.fu.pha.service.impl;

import com.fu.pha.dto.response.report.product.ExpiredProductDto;
import com.fu.pha.dto.response.report.product.OutOfStockProductDto;
import com.fu.pha.entity.ImportItem;
import com.fu.pha.entity.Notification;
import com.fu.pha.entity.Product;
import com.fu.pha.entity.User;
import com.fu.pha.enums.NotificationType;
import com.fu.pha.repository.NotificationRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {


    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void createOutOfStockNotifications(List<OutOfStockProductDto> products) {
        for (OutOfStockProductDto product : products) {
            Notification notification = Notification.builder()
                    .title("Sản phẩm hết hàng")
                    .message("Sản phẩm " + product.getProductName() + " (Mã: " + product.getProductCode() + ") đã hết hàng.")
                    .type(NotificationType.OUT_OF_STOCK)
                    .createdAt(Instant.now())
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        }
    }


    @Override
    public void createLowStockNotifications(List<Product> products) {
        for (Product product : products) {
            Notification notification = Notification.builder()
                    .title("Sản phẩm sắp hết hàng")
                    .message("Sản phẩm " + product.getProductName() + " (Mã: " + product.getProductCode() +
                            ") sắp hết hàng. Số lượng còn lại: " + product.getTotalQuantity() + ".")
                    .type(NotificationType.STOCK_IN_OUT)
                    .createdAt(Instant.now())
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        }
    }

    @Override
    public void createNearlyExpiredProductNotifications(List<ExpiredProductDto> products) {
        Instant fiveDaysAgo = Instant.now().minus(5, ChronoUnit.DAYS);

        for (ExpiredProductDto product : products) {
            // Kiểm tra nếu đã có thông báo trong vòng 5 ngày qua
            boolean hasRecentNotification = notificationRepository.existsByBatchNumberAndTypeAndCreatedAtAfter(
                    product.getBatchNumber(), NotificationType.EXPIRED, fiveDaysAgo);

            if (!hasRecentNotification) {
                // Tạo thông báo mới
                Notification notification = Notification.builder()
                        .title("Sản phẩm sắp hết hạn")
                        .message("Sản phẩm " + product.getProductName() + " (Mã: " + product.getProductCode() +
                                "), Lô: " + product.getBatchNumber() + " sắp hết hạn vào ngày " +
                                product.getExpiryDate() + ".")
                        .type(NotificationType.EXPIRED)
                        .createdAt(Instant.now())
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);
            }
        }
    }


    @Override
    public void createExpiredProductNotifications(List<ImportItem> expiredProducts) {
        for (ImportItem importItem : expiredProducts) {
            Notification notification = Notification.builder()
                    .title("Sản phẩm đã hết hạn")
                    .message("Lô sản phẩm " + importItem.getProduct().getProductName() +
                            " (Mã: " + importItem.getProduct().getProductCode() + "), Lô: " +
                            importItem.getBatchNumber() + " đã hết hạn vào ngày " +
                            importItem.getExpiryDate() + ".")
                    .type(NotificationType.EXPIRED)
                    .createdAt(Instant.now())
                    .isRead(false)
                    .importItem(importItem)
                    .build();
            notificationRepository.save(notification);
        }
    }


    @Override
    public void sendNotificationToUser(String title, String message, User user) {
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(NotificationType.STOCK_IN_OUT)
                .createdAt(Instant.now())
                .isRead(false)
                .user(user)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public Page<OutOfStockProductDto> getOutOfStockProducts(Long categoryId, String searchText, int pageNumber, int pageSize) {
        return productRepository.findOutOfStockProducts(categoryId, searchText, PageRequest.of(pageNumber, pageSize));
    }


    @Override
    public Page<ExpiredProductDto> getExpiredProducts(Long categoryId, String searchText, int warningDays, int pageNumber, int pageSize) {
        Instant currentDate = Instant.now();
        Instant warningDate = currentDate.plus(Duration.ofDays(warningDays));
        return productRepository.findExpiredProducts(categoryId, searchText, warningDate, PageRequest.of(pageNumber, pageSize));
    }



}
