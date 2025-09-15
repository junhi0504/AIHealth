package com.example.AIHealth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // uploads/board 폴더를 /board/uploads/board/** URL로 접근 가능
        registry.addResourceHandler("/board/uploads/board/**")
                .addResourceLocations("file:uploads/board/");
    }
}
