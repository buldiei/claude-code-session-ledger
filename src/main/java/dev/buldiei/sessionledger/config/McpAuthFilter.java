package dev.buldiei.sessionledger.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Guards the MCP endpoint with a static bearer token (Claude's write side).
 * If {@code session-ledger.mcp.token} is blank (local dev), the guard is disabled.
 * The web REST API under /api stays unauthenticated (LAN read/delete).
 */
@Component
public class McpAuthFilter extends OncePerRequestFilter {

    @Value("${session-ledger.mcp.token:}")
    private String token;

    @Value("${session-ledger.mcp.path:/mcp}")
    private String mcpPath;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // only guard the MCP path, and only when a token is configured
        return !StringUtils.hasText(token) || !request.getRequestURI().startsWith(mcpPath);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String expected = "Bearer " + token;
        if (header == null || !constantTimeEquals(header, expected)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid MCP token");
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
