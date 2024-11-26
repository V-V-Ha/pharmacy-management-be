package com.fu.pha.repository;

import com.fu.pha.entity.Notification;
import com.fu.pha.enums.NotificationType;
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
}
