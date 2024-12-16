package com.fu.pha.service;


import com.fu.pha.dto.response.NotificationDTO;
import com.fu.pha.dto.response.report.product.ExpiredProductDto;
import com.fu.pha.dto.response.report.product.OutOfStockProductDto;
import com.fu.pha.entity.ImportItem;
import com.fu.pha.entity.Product;
import com.fu.pha.entity.User;
import com.fu.pha.enums.NotificationType;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {

    void createOutOfStockNotifications(List<OutOfStockProductDto> products);

    void createLowStockNotifications(List<Product> products);
    void sendNotificationToUser(String title, String message, User user, String url);

    void createNearlyExpiredProductNotifications(List<ExpiredProductDto> products);
    void createExpiredProductNotifications(List<ImportItem> expiredProducts);

    void markNotificationAsRead(Long notificationId);

    Page<OutOfStockProductDto> getOutOfStockProducts1(Long categoryId, String searchText, int pageNumber, int pageSize);

    Page<OutOfStockProductDto> getOutOfStockProducts(Long categoryId, String searchText, int pageNumber, int pageSize);

    Page<ExpiredProductDto> getExpiredProducts(Long categoryId, String searchText, int warningDays, int pageNumber, int pageSize);

    Page<NotificationDTO> getRecentNotifications(User user, NotificationType notificationType, Boolean isRead, int pageNumber, int pageSize);
}
