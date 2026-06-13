package dev.buldiei.sessionledger.adapter.in.web.dto;

import dev.buldiei.sessionledger.domain.model.Session;
import dev.buldiei.sessionledger.domain.model.SessionVersion;

import java.time.Instant;
import java.util.List;

/** Card as shown in the list view. */
public record SessionSummaryResponse(
        String sessionId,
        String projectDir,
        String status,
        String title,
        String latestShortDescription,
        List<String> tags,
        int versionCount,
        Instant createdAt,
        Instant updatedAt,
        String resumeCommand
) {
    public static SessionSummaryResponse from(Session s) {
        SessionVersion tip = s.tip();
        return new SessionSummaryResponse(
                s.getSessionId(),
                s.getProjectDir(),
                s.getStatus().name(),
                tip == null ? null : tip.getTitle(),
                tip == null ? null : tip.getShortDescription(),
                tip == null || tip.getTags() == null ? List.of() : tip.getTags(),
                s.versionCount(),
                s.getCreatedAt(),
                s.getUpdatedAt(),
                ResumeCommand.of(s.getProjectDir(), s.getSessionId())
        );
    }
}
