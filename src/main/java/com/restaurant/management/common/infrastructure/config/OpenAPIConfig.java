package com.restaurant.management.common.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAPI (Swagger) 配置类
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("餐厅管理系统 API 文档")
                        .version("1.0.0")
                        .description("基于 DDD 架构的餐厅管理系统 RESTful API 文档")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@restaurant.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(createServers(                ));
    }

    private List<Server> createServers() {
        List<Server> servers = new ArrayList<>();
        servers.add(new Server()
                .url("http://localhost:8080")
                .description("本地开发环境"));
        servers.add(new Server()
                .url("https://api.restaurant.com")
                .description("生产环境"));
        return servers;
    }
}

