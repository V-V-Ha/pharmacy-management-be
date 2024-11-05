package com.fu.pha.service;

import org.springframework.stereotype.Component;

@Component
public class NotificationService {
    public void sendNotification(String title, String message) {
        // Giả lập logic gửi thông báo (email, SMS, thông báo hệ thống, v.v.)
        System.out.println("Thông báo: " + title);
        System.out.println("Nội dung: " + message);
    }

}
