package dev.buldiei.sessionledger.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface SessionJpaRepository extends JpaRepository<SessionJpaEntity, String> {
    List<SessionJpaEntity> findAllByOrderByUpdatedAtDesc();
}
