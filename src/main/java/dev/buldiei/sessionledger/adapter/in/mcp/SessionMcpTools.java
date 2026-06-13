package dev.buldiei.sessionledger.adapter.in.mcp;

import dev.buldiei.sessionledger.domain.model.SessionVersion;
import dev.buldiei.sessionledger.domain.port.in.SaveSessionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * MCP tools exposed to Claude. This is the only write surface (C + U):
 * an unknown session id creates a card, a known one appends a new version to the
 * top of its history graph. There is deliberately no delete/read tool here.
 */
@Component
@RequiredArgsConstructor
public class SessionMcpTools {

    private final SaveSessionUseCase saveSession;

    // Optional explicit public URL. When blank, the card link is derived from the host the
    // MCP request actually came in on — so it's correct without configuration (and immune to a
    // stray .env). Set it only behind a reverse proxy or when the public URL differs.
    @Value("${session-ledger.web-base-url:}")
    private String webBaseUrl;

    @Tool(name = "save_session",
            description = """
                    Save or update the current Claude Code session as a card in the session ledger.
                    Call this for the /save skill. If the session_id is new, a card is created;
                    if it already exists, a new version is appended to the top of its history graph
                    (previous versions are preserved). Provide a short contextual title (a few words),
                    one concise short_description sentence, and optionally a richer long_description
                    and tags.""")
    public String saveSession(
            @ToolParam(description = "The Claude Code session id (the ${CLAUDE_SESSION_ID} value).")
            String sessionId,
            @ToolParam(description = "Absolute working directory the session runs in (cwd), used to build the resume command.")
            String projectDir,
            @ToolParam(description = "Short, contextual card name — a few words capturing what the session was about, e.g. 'Standalone install + /save fixes'. Don't prefix it with the project name (the working directory already shows it). Shown as the card heading.")
            String title,
            @ToolParam(description = "One concise sentence describing the session, shown under the title as a preview.")
            String shortDescription,
            @ToolParam(required = false, description = "Optional longer context/notes for future reference.")
            String longDescription,
            @ToolParam(required = false, description = "Optional list of technologies/topics for this session, e.g. [\"Java 25\", \"Spring Boot 4\", \"Postgres\"]. Shown as chips and used for search.")
            List<String> tags) {

        SessionVersion saved = saveSession.save(SaveSessionUseCase.SaveSessionCommand.builder()
                .sessionId(sessionId)
                .projectDir(projectDir)
                .title(title)
                .shortDescription(shortDescription)
                .longDescription(longDescription)
                .tags(tags)
                .build());

        String url = resolveBaseUrl() + "/sessions/" + sessionId;
        return "Saved version #" + saved.getSeq() + " for session " + sessionId + ". View: " + url;
    }

    /** Configured base URL if set, otherwise the host the current request came in on. */
    private String resolveBaseUrl() {
        if (StringUtils.hasText(webBaseUrl)) {
            return webBaseUrl.replaceAll("/+$", "");
        }
        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        } catch (IllegalStateException noRequestContext) {
            return "http://localhost:8080";
        }
    }
}
