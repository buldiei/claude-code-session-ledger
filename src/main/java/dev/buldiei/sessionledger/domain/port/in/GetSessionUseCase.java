package dev.buldiei.sessionledger.domain.port.in;

import dev.buldiei.sessionledger.domain.model.Session;

import java.util.Optional;

/** Inbound port for the web read side: fetch a single card with its full version graph. */
public interface GetSessionUseCase {
    Optional<Session> getById(String sessionId);
}
