package com.fu.pha.configuration;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Cloudinaryconfig {
    @Bean
    public Cloudinary cloudinary() {
        final Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dmz6hapvj");
        config.put("api_key", "359211291938856");
        config.put("api_secret", "D9fANGl5fK9Q1QGrwBv88fMtIY4");
        return new Cloudinary(config);
    }
}
