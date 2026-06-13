package dev.buldiei.sessionledger.application.service;

import dev.buldiei.sessionledger.domain.port.in.DeleteSessionUseCase;
import dev.buldiei.sessionledger.domain.port.out.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Delete side for the web UI.
 */
@Service
@RequiredArgsConstructor
public class DeleteSessionService implements DeleteSessionUseCase {

    private final SessionRepository repository;

    @Override
    @Transactional
    public boolean deleteById(String sessionId) {
        return repository.deleteById(sessionId);
    }
}
