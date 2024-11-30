package com.fu.pha.repository;

import com.fu.pha.entity.Notification;
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

    // Tìm thông báo trong vòng 6 ngày gần nhất theo loại với phân trang
    @Query("SELECT n FROM Notification n WHERE n.createdAt >= :sixDaysAgo AND n.type = :notificationType ORDER BY n.createdAt DESC")
    Page<Notification> findRecentNotificationsByType(Instant sixDaysAgo, NotificationType notificationType, Pageable pageable);

    // Tìm tất cả thông báo trong vòng 6 ngày gần nhất với phân trang
    @Query("SELECT n FROM Notification n WHERE n.createdAt >= :sixDaysAgo ORDER BY n.createdAt DESC")
    Page<Notification> findRecentNotifications(Instant sixDaysAgo, Pageable pageable);


}
