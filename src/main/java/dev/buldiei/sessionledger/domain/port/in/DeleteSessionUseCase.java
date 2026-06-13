package dev.buldiei.sessionledger.domain.port.in;

/** Inbound port for the web delete side. */
public interface DeleteSessionUseCase {
    /** @return true if a session was deleted, false if it did not exist. */
    boolean deleteById(String sessionId);
}
