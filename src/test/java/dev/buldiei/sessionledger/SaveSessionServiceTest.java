package dev.buldiei.sessionledger;

import dev.buldiei.sessionledger.application.service.SaveSessionService;
import dev.buldiei.sessionledger.domain.model.Session;
import dev.buldiei.sessionledger.domain.model.SessionStatus;
import dev.buldiei.sessionledger.domain.model.SessionVersion;
import dev.buldiei.sessionledger.domain.port.in.SaveSessionUseCase.SaveSessionCommand;
import dev.buldiei.sessionledger.domain.port.out.SessionRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Pure unit test of the create-or-append behaviour with an in-memory fake repository. */
class SaveSessionServiceTest {

    private final FakeRepo repo = new FakeRepo();
    private final SaveSessionService service = new SaveSessionService(repo);

    @Test
    void firstSaveCreatesSessionAndSeqOne() {
        SessionVersion v = service.save(cmd("First context."));

        assertThat(repo.sessions).containsKey("s1");
        assertThat(v.getSeq()).isEqualTo(1);
        assertThat(v.getParentVersionId()).isNull();
    }

    @Test
    void secondSaveAppendsVersionLinkedToPreviousTip() {
        SessionVersion first = service.save(cmd("First."));
        SessionVersion second = service.save(cmd("Second, evolved."));

        assertThat(second.getSeq()).isEqualTo(2);
        assertThat(second.getParentVersionId()).isEqualTo(first.getId());
        assertThat(repo.versions).hasSize(2);
    }

    private SaveSessionCommand cmd(String shortDesc) {
        return SaveSessionCommand.builder()
                .sessionId("s1")
                .projectDir("/tmp/proj")
                .shortDescription(shortDesc)
                .build();
    }

    /** Minimal in-memory SessionRepository for the unit test. */
    static class FakeRepo implements SessionRepository {
        final java.util.Map<String, Session> sessions = new java.util.HashMap<>();
        final List<SessionVersion> versions = new ArrayList<>();

        @Override
        public Optional<Session> findById(String id) {
            if (!sessions.containsKey(id)) return Optional.empty();
            List<SessionVersion> own = versions.stream().filter(v -> v.getSessionId().equals(id)).toList();
            Session base = sessions.get(id);
            return Optional.of(Session.builder()
                    .sessionId(base.getSessionId())
                    .projectDir(base.getProjectDir())
                    .status(base.getStatus())
                    .createdAt(base.getCreatedAt())
                    .updatedAt(base.getUpdatedAt())
                    .versions(own)
                    .build());
        }

        @Override public List<Session> findAll() { return List.copyOf(sessions.values()); }
        @Override public boolean existsById(String id) { return sessions.containsKey(id); }

        @Override public void createSession(Session s) {
            sessions.put(s.getSessionId(), Session.builder()
                    .sessionId(s.getSessionId()).projectDir(s.getProjectDir())
                    .status(SessionStatus.ACTIVE).createdAt(Instant.now()).updatedAt(Instant.now())
                    .versions(List.of()).build());
        }

        @Override public void touchUpdatedAt(String id) { /* no-op in test */ }

        @Override public SessionVersion appendVersion(SessionVersion v) {
            SessionVersion stored = SessionVersion.builder()
                    .id(v.getId()).sessionId(v.getSessionId()).parentVersionId(v.getParentVersionId())
                    .seq(v.getSeq()).shortDescription(v.getShortDescription())
                    .longDescription(v.getLongDescription()).createdAt(Instant.now()).build();
            versions.add(stored);
            return stored;
        }

        @Override public boolean deleteById(String id) { return sessions.remove(id) != null; }
    }
}
