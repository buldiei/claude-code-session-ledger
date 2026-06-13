package dev.buldiei.sessionledger.application.service;

import dev.buldiei.sessionledger.domain.model.Session;
import dev.buldiei.sessionledger.domain.port.in.GetSessionUseCase;
import dev.buldiei.sessionledger.domain.port.in.ListSessionsUseCase;
import dev.buldiei.sessionledger.domain.port.out.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Read side for the web UI.
 */
@Service
@RequiredArgsConstructor
public class QuerySessionService implements ListSessionsUseCase, GetSessionUseCase {

    private final SessionRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<Session> listAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Session> getById(String sessionId) {
        return repository.findById(sessionId);
    }
}
