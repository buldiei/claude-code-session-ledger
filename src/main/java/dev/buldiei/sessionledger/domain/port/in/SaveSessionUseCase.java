package dev.buldiei.sessionledger.domain.port.in;

import dev.buldiei.sessionledger.domain.model.SessionVersion;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Inbound port used by the MCP adapter (Claude's C/U side).
 * Upserts a session and appends a new version node. First call creates the card;
 * subsequent calls for the same session id append to its history graph.
 */
public interface SaveSessionUseCase {

    SessionVersion save(SaveSessionCommand command);

    @Value
    @Builder
    class SaveSessionCommand {
        @NonNull String sessionId;
        @NonNull String projectDir;
        String title;           // short contextual name
        @NonNull String shortDescription;
        String longDescription; // optional
        List<String> tags;      // optional technologies/topics
    }
}
