package com.fu.pha.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        logger.info("Đang khởi tạo Firebase...");

        FileInputStream serviceAccount =
                new FileInputStream("src/main/resources/pharmacy-management-e6dd2-firebase-adminsdk-8bp3f-52aeed4337.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
            logger.info("Firebase đã được khởi tạo thành công.");
        } else {
            logger.info("Firebase đã được khởi tạo trước đó.");
        }

        return FirebaseApp.getInstance();
    }
}

