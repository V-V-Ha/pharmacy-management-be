package com.fu.pha.repository;

import com.fu.pha.entity.Notification;
import com.fu.pha.entity.User;
import com.fu.pha.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT COUNT(n) > 0 FROM Notification n " +
            "WHERE n.importItem.batchNumber = :batchNumber " +
            "AND n.type = :type " +
            "AND n.createdAt >= :since")
    boolean existsByBatchNumberAndTypeAndCreatedAtAfter(@Param("batchNumber") String batchNumber,
                                                        @Param("type") NotificationType type,
                                                        @Param("since") Instant since);

    List<Notification> findByUserId(Long userId);

    // Lọc các thông báo đã đọc, theo loại, theo người dùng và trong 6 ngày gần nhất
    Page<Notification> findRecentNotificationsByTypeAndIsReadAndUser(
            NotificationType type, User user, Boolean isRead, Instant createdAt, Pageable pageable);

    // Lọc các thông báo chưa đọc của người dùng
    Page<Notification> findRecentNotificationsByIsReadAndUser(
            Boolean isRead, User user, Pageable pageable);

    // Lọc các thông báo đã đọc trong 6 ngày gần nhất của người dùng
    @Query("SELECT n FROM Notification n WHERE n.user = :user " +
            "AND (:notificationType IS NULL OR n.type = :notificationType) " +
            "AND (:isRead IS NULL OR n.isRead = :isRead OR (:isRead = true AND n.createdAt > :sixDaysAgo))")
    Page<Notification> findNotifications(
            @Param("user") User user,
            @Param("notificationType") NotificationType notificationType,
            @Param("sixDaysAgo") Instant sixDaysAgo,
            @Param("isRead") Boolean isRead,  // Allowing isRead to be null
            Pageable pageable);
}
