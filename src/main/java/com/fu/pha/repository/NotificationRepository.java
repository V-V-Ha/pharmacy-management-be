package com.fu.pha.repository;

import com.fu.pha.entity.Notification;
import com.fu.pha.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    // Lọc theo loại thông báo và đã đọc trong 6 ngày gần nhất
    Page<Notification> findRecentNotificationsByTypeAndIsRead(
            Instant sixDaysAgo, NotificationType notificationType, boolean isRead, PageRequest pageRequest);

    // Lọc tất cả các thông báo chưa đọc (không giới hạn theo thời gian)
    Page<Notification> findRecentNotificationsByIsRead(boolean isRead, PageRequest pageRequest);

    // Lọc các thông báo đã đọc trong 6 ngày gần nhất
    Page<Notification> findRecentNotificationsByIsReadAndCreatedAt(
            boolean isRead, Instant sixDaysAgo, PageRequest pageRequest);


}
