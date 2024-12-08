package com.fu.pha.service.impl;

import com.fu.pha.dto.response.NotificationDTO;
import com.fu.pha.dto.response.report.product.ExpiredProductDto;
import com.fu.pha.dto.response.report.product.OutOfStockProductDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.NotificationType;
import com.fu.pha.exception.Message;
import com.fu.pha.repository.NotificationRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.repository.UserRepository;
import com.fu.pha.service.FirebaseNotificationService;
import com.fu.pha.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {


    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FirebaseNotificationService firebaseNotificationService;

    public List<User> getProductOwners() {
        return userRepository.findAllByRoles_NameIn(Arrays.asList(ERole.ROLE_PRODUCT_OWNER, ERole.ROLE_STOCK));
    }

    @Override
    public void createOutOfStockNotifications(List<OutOfStockProductDto> products) {
        List<User> productOwners = getProductOwners();
        for (OutOfStockProductDto product : products) {
            Product productEntity = productRepository.findById(product.getProductId())
                    .orElseThrow(() -> new RuntimeException(Message.PRODUCT_NOT_FOUND));
            for (User user : productOwners) {
                String url = "/report/inventory/outOfStock";

                // Tạo thông báo trong cơ sở dữ liệu
                Notification notification = Notification.builder()
                        .title("Sản phẩm hết hàng")
                        .message("Sản phẩm " + product.getProductName() + " (Mã: " + product.getProductCode() + ") đã hết hàng.")
                        .type(NotificationType.OUT_OF_STOCK)
                        .createdAt(Instant.now())
                        .user(user)
                        .product(productEntity)
                        .isRead(false)
                        .url(url)
                        .build();
                notificationRepository.save(notification);

                // Gửi thông báo FCM cho người dùng
                firebaseNotificationService.sendNotification(
                        notification.getTitle(),
                        notification.getMessage(),
                        url,
                        user.getFcmToken(),  // Token FCM của người dùng
                        notification.getId()
                );
            }
        }
    }


    @Override
    public void createLowStockNotifications(List<Product> products) {
        List<User> productOwners = getProductOwners();
        for (Product product : products) {
            for (User user : productOwners) {
                String url = "/report/inventory/outOfStock";
                // Tạo thông báo trong cơ sở dữ liệu
                Notification notification = Notification.builder()
                        .title("Sản phẩm sắp hết hàng")
                        .message("Sản phẩm " + product.getProductName() + " (Mã: " + product.getProductCode() +
                                ") sắp hết hàng. Số lượng còn lại: " + product.getTotalQuantity() + ".")
                        .type(NotificationType.OUT_OF_STOCK)
                        .createdAt(Instant.now())
                        .user(user)
                        .product(product)
                        .isRead(false)
                        .url(url)
                        .build();
                notificationRepository.save(notification);

                // Gửi thông báo FCM cho người dùng
                firebaseNotificationService.sendNotification(
                        notification.getTitle(),
                        notification.getMessage(),
                        url,
                        user.getFcmToken(),  // Token FCM của người dùng
                        notification.getId()
                );
            }
        }
    }

    @Override
    public void createNearlyExpiredProductNotifications(List<ExpiredProductDto> products) {
        Instant fiveDaysAgo = Instant.now().minus(5, ChronoUnit.DAYS);
        List<User> productOwners = getProductOwners();
        for (ExpiredProductDto product : products) {
            String url = "/report/inventory/expired";
            // Kiểm tra nếu đã có thông báo trong vòng 5 ngày qua
            boolean hasRecentNotification = notificationRepository.existsByBatchNumberAndTypeAndCreatedAtAfter(
                    product.getBatchNumber(), NotificationType.EXPIRED, fiveDaysAgo);

            Product productEntity = productRepository.findById(product.getProductId())
                    .orElseThrow(() -> new RuntimeException(Message.PRODUCT_NOT_FOUND));

            if (!hasRecentNotification) {
                // Tạo thông báo trong cơ sở dữ liệu
                for (User user : productOwners) {
                    Notification notification = Notification.builder()
                            .title("Sản phẩm sắp hết hạn")
                            .message("Sản phẩm " + product.getProductName() + " (Mã: " + product.getProductCode() +
                                    "), Lô: " + product.getBatchNumber() + " sắp hết hạn vào ngày " +
                                    product.getExpiryDate() + ".")
                            .type(NotificationType.EXPIRED)
                            .createdAt(Instant.now())
                            .user(user)
                            .product(productEntity)
                            .isRead(false)
                            .url(url)
                            .build();
                    notificationRepository.save(notification);

                    // Gửi thông báo FCM cho người dùng
                    firebaseNotificationService.sendNotification(
                            notification.getTitle(),
                            notification.getMessage(),
                            url,
                            user.getFcmToken(),  // Token FCM của người dùng
                            notification.getId()
                    );
                }
            }
        }
    }


    @Override
    public void createExpiredProductNotifications(List<ImportItem> expiredProducts) {
        List<User> productOwners = getProductOwners();
        for (ImportItem importItem : expiredProducts) {
            Product productEntity = productRepository.findById(importItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException(Message.PRODUCT_NOT_FOUND));
            for (User user : productOwners) {
                String url = "/report/inventory/expired";
                // Tạo thông báo trong cơ sở dữ liệu
                Notification notification = Notification.builder()
                        .title("Sản phẩm đã hết hạn")
                        .message("Lô sản phẩm " + importItem.getProduct().getProductName() +
                                " (Mã: " + importItem.getProduct().getProductCode() + "), Lô: " +
                                importItem.getBatchNumber() + " đã hết hạn vào ngày " +
                                importItem.getExpiryDate() + ".")
                        .type(NotificationType.EXPIRED)
                        .createdAt(Instant.now())
                        .user(user)
                        .product(productEntity)
                        .importItem(importItem)
                        .isRead(false)
                        .url(url)
                        .build();
                notificationRepository.save(notification);

                // Gửi thông báo FCM cho người dùng
                firebaseNotificationService.sendNotification(
                        notification.getTitle(),
                        notification.getMessage(),
                        url,
                        user.getFcmToken(),  // Token FCM của người dùng
                        notification.getId()
                );
            }
        }
    }



    @Override
    public void sendNotificationToUser(String title, String message, User user, String url) {
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(NotificationType.STOCK_IN_OUT)
                .createdAt(Instant.now())
                .isRead(false)
                .url(url)
                .user(user)
                .build();
        notificationRepository.save(notification);

        // Gửi thông báo FCM cho người dùng
        firebaseNotificationService.sendNotification(
                notification.getTitle(),
                notification.getMessage(),
                url,
                user.getFcmToken(),  // Token FCM của người dùng
                notification.getId()
        );
    }

    @Override
    public void markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
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


    // Get all notifications by user
    @Override
    public Page<NotificationDTO> getRecentNotifications(User user, NotificationType notificationType, Boolean isRead, int pageNumber, int pageSize) {
        Instant sixDaysAgo = Instant.now().minus(6, ChronoUnit.DAYS);

        // Tạo Pageable cho phân trang
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        // Gọi repository để lấy dữ liệu với các điều kiện lọc trong một query duy nhất
        Page<Notification> notificationsPage = notificationRepository.findNotifications(
                user, notificationType, sixDaysAgo, isRead, pageRequest);

        // Chuyển đổi từ Page<Notification> sang Page<NotificationDTO>
        return notificationsPage.map(notification -> new NotificationDTO(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getType(),
                notification.getIsRead(),
                notification.getUrl()
        ));
    }



}
