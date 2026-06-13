package dev.buldiei.sessionledger.domain.port.in;

import dev.buldiei.sessionledger.domain.model.Session;

import java.util.List;

/** Inbound port for the web read side: list all session cards, newest activity first. */
public interface ListSessionsUseCase {
    List<Session> listAll();
}
