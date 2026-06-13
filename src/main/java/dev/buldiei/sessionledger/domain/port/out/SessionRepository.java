package dev.buldiei.sessionledger.domain.port.out;

import dev.buldiei.sessionledger.domain.model.Session;
import dev.buldiei.sessionledger.domain.model.SessionVersion;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port. The persistence adapter implements this; the domain stays free of JPA.
 */
public interface SessionRepository {

    Optional<Session> findById(String sessionId);

    List<Session> findAll();

    boolean existsById(String sessionId);

    /** Create the card head (first save). */
    void createSession(Session session);

    /** Touch updated_at after a new version is appended. */
    void touchUpdatedAt(String sessionId);

    /** Append a version node and return it as persisted. */
    SessionVersion appendVersion(SessionVersion version);

    /** @return true if a row was removed. */
    boolean deleteById(String sessionId);
}
