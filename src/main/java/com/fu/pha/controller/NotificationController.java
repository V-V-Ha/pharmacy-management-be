package com.fu.pha.controller;

import com.fu.pha.dto.response.NotificationDTO;
import com.fu.pha.entity.Notification;
import com.fu.pha.enums.NotificationType;
import com.fu.pha.repository.NotificationRepository;
import com.fu.pha.service.FirebaseNotificationService;
import com.fu.pha.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;



    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markNotificationAsRead(id);
        return ResponseEntity.ok("Thông báo đã đọc");
    }

    @GetMapping("/{userId}")
    public List<Notification> getNotifications(@PathVariable Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @RequestParam(value = "type", required = false) NotificationType notificationType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Page<NotificationDTO> notificationsPage = notificationService.getRecentNotifications(notificationType, page, size);
        return ResponseEntity.ok(notificationsPage);
    }


}