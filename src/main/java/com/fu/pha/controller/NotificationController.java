package com.fu.pha.controller;

import com.fu.pha.entity.Notification;
import com.fu.pha.repository.NotificationRepository;
import com.fu.pha.service.FirebaseNotificationService;
import com.fu.pha.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
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


    private final FirebaseNotificationService firebaseNotificationService;

    @Autowired
    public NotificationController(FirebaseNotificationService firebaseNotificationService) {
        this.firebaseNotificationService = firebaseNotificationService;
    }

    @PostMapping("/send")
    public String sendNotification(@RequestParam String title,
                                   @RequestParam String body,
                                   @RequestParam String token,
                                   @RequestParam Long notificationId) {
        firebaseNotificationService.sendNotification(title, body, token, notificationId);
        return "Notification sent!";
    }

}