package dev.buldiei.sessionledger.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * One node in a session's vertical history graph.
 * Each {@code /save} appends a new version; {@code parentVersionId} points to the
 * previous tip, so the chain reads bottom-up with the latest node on top.
 */
@Value
@Builder
public class SessionVersion {

    UUID id;
    String sessionId;
    UUID parentVersionId;   // null for the first version
    int seq;                // 1-based
    String title;           // short contextual card name (a few words)
    String shortDescription;
    String longDescription; // nullable
    List<String> tags;      // technologies/topics, possibly empty
    Instant createdAt;
}
