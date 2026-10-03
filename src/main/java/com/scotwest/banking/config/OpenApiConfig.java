package com.scotwest.banking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ScotWest Bank Enterprise Core API")
                        .description("Production-grade core banking backend covering Customer Management, Account Services, Atomic Transfers, and Audit Ledger.")
                        .version("v1.0.0")
                        .contact(new Contact().name("ScotWest Engineering Team").email("dev@scotwest.co.uk"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}
