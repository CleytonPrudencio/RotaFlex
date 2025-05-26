package br.com.rotaflex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // aceita todas as origens
                .allowedMethods("*")        // aceita todos os métodos: GET, POST, PUT, DELETE, etc.
                .allowedHeaders("*")        // aceita todos os headers
                .allowCredentials(true);    // permite envio de cookies/autenticação (se necessário)
    }
}



