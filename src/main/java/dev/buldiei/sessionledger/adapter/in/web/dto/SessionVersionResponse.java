package dev.buldiei.sessionledger.adapter.in.web.dto;

import dev.buldiei.sessionledger.domain.model.SessionVersion;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** One node of the vertical history graph. */
public record SessionVersionResponse(
        UUID id,
        UUID parentVersionId,
        int seq,
        String title,
        String shortDescription,
        String longDescription,
        List<String> tags,
        Instant createdAt
) {
    public static SessionVersionResponse from(SessionVersion v) {
        return new SessionVersionResponse(
                v.getId(),
                v.getParentVersionId(),
                v.getSeq(),
                v.getTitle(),
                v.getShortDescription(),
                v.getLongDescription(),
                v.getTags() == null ? List.of() : v.getTags(),
                v.getCreatedAt()
        );
    }
}
