package com.x1.groo.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {

        Info info = new Info()
                .version("v1.0.0")
                .title("USER-SERVICE API 명세서")
                .description("회원 기능에 관한 API 명세서");

        return new OpenAPI()
                .info(info);
    }
}
