package com.fu.pha.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("src/main/resources/pharmacy-management-e6dd2-firebase-adminsdk-8bp3f-52aeed4337.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize Firebase", e);
        }
    }
}
