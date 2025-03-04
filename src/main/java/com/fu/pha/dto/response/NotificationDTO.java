package com.fu.pha.dto.response;

import com.fu.pha.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private Instant createdAt;
    private NotificationType type;
    private Boolean isRead;
    private String url;
}