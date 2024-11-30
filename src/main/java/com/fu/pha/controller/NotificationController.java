package com.fu.pha.controller;

import com.fu.pha.dto.response.NotificationDTO;
import com.fu.pha.entity.Notification;
import com.fu.pha.entity.User;
import com.fu.pha.enums.NotificationType;
import com.fu.pha.exception.Message;
import com.fu.pha.repository.NotificationRepository;
import com.fu.pha.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;



    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markNotificationAsRead(id);
        return ResponseEntity.ok("Thông báo đã đọc");
    }


    @GetMapping("notification/user/{userId}")
    public ResponseEntity<Page<NotificationDTO>> getRecentNotifications(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "notificationType", required = false) NotificationType notificationType,
            @RequestParam(name = "isRead", required = false) Boolean isRead,
            @RequestParam(name = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(Message.USER_NOT_FOUND));
        
        Page<NotificationDTO> notifications = notificationService.getRecentNotifications(
                user, notificationType, isRead, pageNumber, pageSize);

        return ResponseEntity.ok(notifications);
    }


}