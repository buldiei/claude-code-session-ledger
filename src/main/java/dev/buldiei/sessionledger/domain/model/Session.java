package dev.buldiei.sessionledger.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

/**
 * Aggregate root: a Claude Code session represented as a card with a chain of versions.
 * {@code versions} is ordered oldest-first; the tip (latest) is the last element.
 */
@Value
@Builder
public class Session {

    String sessionId;
    String projectDir;
    SessionStatus status;
    Instant createdAt;
    Instant updatedAt;
    List<SessionVersion> versions;

    /** The current (latest) version, i.e. the top of the vertical graph. */
    public SessionVersion tip() {
        if (versions == null || versions.isEmpty()) {
            return null;
        }
        return versions.getLast();
    }

    public int versionCount() {
        return versions == null ? 0 : versions.size();
    }
}
