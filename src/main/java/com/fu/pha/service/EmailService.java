package com.fu.pha.service;

import com.fu.pha.entity.User;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;

public interface EmailService {
    ResponseEntity<Object> sendSimpleEmail(String to, String subject, String text);
}
