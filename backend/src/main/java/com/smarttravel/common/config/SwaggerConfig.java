package com.smarttravel.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 configuration with JWT Bearer Authentication scheme.
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI smartTravelOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartTravel Platform API")
                        .description("Enterprise REST & Real-Time APIs for SmartTravel Platform — supporting live flight tracking, dynamic pricing, automated refunds, seat/room maps, verified reviews, and hybrid recommendations.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("SmartTravel Engineering Team")
                                .email("engineering@smarttravel.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer Token (format: 'Bearer <token>')")));
    }
}
