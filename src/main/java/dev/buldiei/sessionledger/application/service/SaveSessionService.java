package dev.buldiei.sessionledger.application.service;

import dev.buldiei.sessionledger.domain.model.Session;
import dev.buldiei.sessionledger.domain.model.SessionStatus;
import dev.buldiei.sessionledger.domain.model.SessionVersion;
import dev.buldiei.sessionledger.domain.port.in.SaveSessionUseCase;
import dev.buldiei.sessionledger.domain.port.out.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Create-or-update behaviour for Claude's MCP side.
 * Unknown session id -> create the card (C). Known id -> append a version (U).
 * Either way the result is a new node appended to the top of the history graph.
 */
@Service
@RequiredArgsConstructor
public class SaveSessionService implements SaveSessionUseCase {

    private final SessionRepository repository;

    @Override
    @Transactional
    public SessionVersion save(SaveSessionCommand command) {
        Session existing = repository.findById(command.getSessionId()).orElse(null);

        UUID parentId;
        int nextSeq;
        if (existing == null) {
            repository.createSession(Session.builder()
                    .sessionId(command.getSessionId())
                    .projectDir(command.getProjectDir())
                    .status(SessionStatus.ACTIVE)
                    .build());
            parentId = null;
            nextSeq = 1;
        } else {
            SessionVersion tip = existing.tip();
            parentId = tip == null ? null : tip.getId();
            nextSeq = existing.versionCount() + 1;
        }

        SessionVersion toAppend = SessionVersion.builder()
                .id(UUID.randomUUID())
                .sessionId(command.getSessionId())
                .parentVersionId(parentId)
                .seq(nextSeq)
                .title(command.getTitle())
                .shortDescription(command.getShortDescription())
                .longDescription(command.getLongDescription())
                .tags(command.getTags())
                .build();

        SessionVersion saved = repository.appendVersion(toAppend);
        repository.touchUpdatedAt(command.getSessionId());
        return saved;
    }
}
