package dev.buldiei.sessionledger.config;

import dev.buldiei.sessionledger.adapter.in.mcp.SessionMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers {@link SessionMcpTools} with the Spring AI MCP server.
 * The MCP WebMVC starter publishes these over streamable-HTTP at the configured endpoint.
 * <p>
 * NOTE (Spring AI 2.0.0, fresh GA): if 2.0 auto-registers @Tool beans, this provider may be
 * redundant; if the API moved to @McpTool, adjust the annotations in SessionMcpTools. Verify
 * against the 2.0.0 MCP server reference docs on first build.
 */
@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider sessionTools(SessionMcpTools sessionMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(sessionMcpTools)
                .build();
    }
}
