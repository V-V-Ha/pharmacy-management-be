package com.fu.pha;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.scheduling.annotation.EnableScheduling;
import vn.payos.PayOS;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class PharmacyApplication {

	@Value("${payment.payOS.client_id}")
	private String clientId ;
	@Value("${payment.payOS.api_key}")
	private String apiKey;
	@Value("${payment.payOS.checkSum_key}")
	private String checksumKey;

	@Bean
	public PayOS payOS() {
		return new PayOS(clientId, apiKey, checksumKey);
	}

	public static void main(String[] args) {
		SpringApplication.run(PharmacyApplication.class, args);
	}
}
