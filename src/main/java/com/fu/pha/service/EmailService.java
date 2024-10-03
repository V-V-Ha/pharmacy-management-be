package com.fu.pha.service;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String text);
}
