package com.x1.groo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info =@Info(title = "Groo API 명세서",
        description = "Groo에 관한 API 명세서",
        version = "v1.0.0")

)

@Configuration
public class swaggerConfig {

}
