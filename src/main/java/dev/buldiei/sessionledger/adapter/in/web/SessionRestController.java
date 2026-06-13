package dev.buldiei.sessionledger.adapter.in.web;

import dev.buldiei.sessionledger.adapter.in.web.dto.SessionDetailResponse;
import dev.buldiei.sessionledger.adapter.in.web.dto.SessionSummaryResponse;
import dev.buldiei.sessionledger.domain.port.in.DeleteSessionUseCase;
import dev.buldiei.sessionledger.domain.port.in.GetSessionUseCase;
import dev.buldiei.sessionledger.domain.port.in.ListSessionsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Web read/delete side. Intentionally exposes only R and D — creation and updates
 * happen through the MCP adapter (Claude's side).
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionRestController {

    private final ListSessionsUseCase listSessions;
    private final GetSessionUseCase getSession;
    private final DeleteSessionUseCase deleteSession;

    @GetMapping
    public List<SessionSummaryResponse> list() {
        return listSessions.listAll().stream()
                .map(SessionSummaryResponse::from)
                .toList();
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionDetailResponse> get(@PathVariable String sessionId) {
        return getSession.getById(sessionId)
                .map(SessionDetailResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> delete(@PathVariable String sessionId) {
        boolean removed = deleteSession.deleteById(sessionId);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
