package com.fu.pha.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FirebaseNotificationService {

    public void sendNotification(String title, String body, String token, Long notificationId) {
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
//                    .putData("redirectUrl", redirectUrl)
                    .putData("notificationId", String.valueOf(notificationId))
                    .setToken(token)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Sent message: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}