package dev.buldiei.sessionledger.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SessionVersionJpaRepository extends JpaRepository<SessionVersionJpaEntity, UUID> {
    List<SessionVersionJpaEntity> findAllBySessionIdOrderBySeqAsc(String sessionId);

    long deleteBySessionId(String sessionId);
}
