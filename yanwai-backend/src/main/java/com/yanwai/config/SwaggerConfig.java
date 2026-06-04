package com.yanwai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("言外 - 听懂TA的潜台词 API文档")
                        .description("AI对话分析、人格卡牌收集、成就系统")
                        .contact(new Contact().name("开发团队"))
                        .version("1.0.0"));
    }
}
