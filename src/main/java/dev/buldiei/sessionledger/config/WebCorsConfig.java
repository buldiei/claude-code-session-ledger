package dev.buldiei.sessionledger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allow the SvelteKit dev server (and configured origins) to call the REST API.
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Value("${session-ledger.cors.allowed-origins:http://localhost:5173}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "DELETE", "OPTIONS");
    }
}
