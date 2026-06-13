package dev.buldiei.sessionledger.adapter.in.web.dto;

import dev.buldiei.sessionledger.domain.model.Session;

import java.time.Instant;
import java.util.List;

/**
 * Full card with its version graph. {@code versions} is ordered newest-first
 * so the frontend renders the tip at the top of the vertical graph.
 */
public record SessionDetailResponse(
        String sessionId,
        String projectDir,
        String status,
        Instant createdAt,
        Instant updatedAt,
        String resumeCommand,
        List<SessionVersionResponse> versions
) {
    public static SessionDetailResponse from(Session s) {
        List<SessionVersionResponse> versions = s.getVersions().stream()
                .sorted((a, b) -> Integer.compare(b.getSeq(), a.getSeq())) // newest first
                .map(SessionVersionResponse::from)
                .toList();
        return new SessionDetailResponse(
                s.getSessionId(),
                s.getProjectDir(),
                s.getStatus().name(),
                s.getCreatedAt(),
                s.getUpdatedAt(),
                ResumeCommand.of(s.getProjectDir(), s.getSessionId()),
                versions
        );
    }
}
