package dev.buldiei.sessionledger.adapter.out.persistence;

import dev.buldiei.sessionledger.domain.model.Session;
import dev.buldiei.sessionledger.domain.model.SessionVersion;
import dev.buldiei.sessionledger.domain.port.out.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class SessionPersistenceAdapter implements SessionRepository {

    private final SessionJpaRepository sessionRepo;
    private final SessionVersionJpaRepository versionRepo;

    @Override
    public Optional<Session> findById(String sessionId) {
        return sessionRepo.findById(sessionId).map(this::toDomain);
    }

    @Override
    public List<Session> findAll() {
        return sessionRepo.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(String sessionId) {
        return sessionRepo.existsById(sessionId);
    }

    @Override
    public void createSession(Session session) {
        Instant now = Instant.now();
        SessionJpaEntity e = new SessionJpaEntity();
        e.setSessionId(session.getSessionId());
        e.setProjectDir(session.getProjectDir());
        e.setStatus(session.getStatus());
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        sessionRepo.save(e);
    }

    @Override
    public void touchUpdatedAt(String sessionId) {
        sessionRepo.findById(sessionId).ifPresent(e -> {
            e.setUpdatedAt(Instant.now());
            sessionRepo.save(e);
        });
    }

    @Override
    public SessionVersion appendVersion(SessionVersion version) {
        SessionVersionJpaEntity e = new SessionVersionJpaEntity();
        e.setId(version.getId());
        e.setSessionId(version.getSessionId());
        e.setParentVersionId(version.getParentVersionId());
        e.setSeq(version.getSeq());
        e.setTitle(version.getTitle());
        e.setShortDescription(version.getShortDescription());
        e.setLongDescription(version.getLongDescription());
        e.setTags(joinTags(version.getTags()));
        e.setCreatedAt(version.getCreatedAt() != null ? version.getCreatedAt() : Instant.now());
        return toDomain(versionRepo.save(e));
    }

    @Override
    public boolean deleteById(String sessionId) {
        if (!sessionRepo.existsById(sessionId)) {
            return false;
        }
        // Delete children explicitly so behaviour is identical on Postgres and SQLite
        // (SQLite does not enforce FK cascade unless pragma-enabled).
        versionRepo.deleteBySessionId(sessionId);
        sessionRepo.deleteById(sessionId);
        return true;
    }

    private Session toDomain(SessionJpaEntity e) {
        List<SessionVersion> versions = versionRepo
                .findAllBySessionIdOrderBySeqAsc(e.getSessionId()).stream()
                .map(this::toDomain)
                .toList();
        return Session.builder()
                .sessionId(e.getSessionId())
                .projectDir(e.getProjectDir())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .versions(versions)
                .build();
    }

    private SessionVersion toDomain(SessionVersionJpaEntity e) {
        return SessionVersion.builder()
                .id(e.getId())
                .sessionId(e.getSessionId())
                .parentVersionId(e.getParentVersionId())
                .seq(e.getSeq())
                .title(e.getTitle())
                .shortDescription(e.getShortDescription())
                .longDescription(e.getLongDescription())
                .tags(splitTags(e.getTags()))
                .createdAt(e.getCreatedAt())
                .build();
    }

    private static String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .reduce((a, b) -> a + "," + b)
                .orElse(null);
    }

    private static List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .toList();
    }
}
