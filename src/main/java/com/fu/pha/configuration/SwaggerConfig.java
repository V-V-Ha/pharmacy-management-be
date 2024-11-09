package com.fu.pha.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .servers(Collections.singletonList(new Server().url("http://localhost:8080")))
                .info(new Info().title("SEP490-G27-Pha")
                        .version("1.0.0"));
    }
}
//http://localhost:8080/swagger-ui.html
//Https://croak-order.click/login
//Http://139.180.155.250:8080/swagger-ui.html
//http://139.180.155.250:8000/view/dashboard
//Usr: root
//PW: 3%My{pgDtN*UK,SV


//  Ticchs hop thanh toan
// Client ID : 26876906-65fd-4d08-afd2-c19fcada6857
// API Key : 4fe2a0c9-5986-4150-be96-263684e0014f
// Checksum Key : ee3386799b1e49366fb904e05b8cf21ce13b682e188b805513c0c305696293e3

