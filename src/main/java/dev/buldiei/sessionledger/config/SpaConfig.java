package dev.buldiei.sessionledger.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Serves the built SvelteKit SPA (copied into classpath:/static/ at image build time)
 * from the same origin as the API and MCP endpoint — one URL, no CORS in production.
 *
 * Real files are served as-is; any unknown client-side route (e.g. /sessions/{id} on a
 * hard refresh) falls back to index.html. Requests under /api and /mcp are left to their
 * own handlers.
 */
@Configuration
public class SpaConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) {
                        if (resourcePath.startsWith("api/") || resourcePath.startsWith("mcp")) {
                            return null; // not ours — let the REST/MCP handlers deal with it
                        }
                        try {
                            Resource requested = location.createRelative(resourcePath);
                            if (requested.exists() && requested.isReadable()) {
                                return requested;
                            }
                        } catch (Exception ignored) {
                            // fall through to SPA index
                        }
                        Resource index = new ClassPathResource("/static/index.html");
                        return index.exists() ? index : null;
                    }
                });
    }
}
