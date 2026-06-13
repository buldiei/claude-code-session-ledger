package dev.buldiei.sessionledger.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "session_version")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionVersionJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "parent_version_id")
    private UUID parentVersionId;

    @Column(name = "seq", nullable = false)
    private int seq;

    @Column(name = "title")
    private String title;

    @Column(name = "short_description", nullable = false)
    private String shortDescription;

    @Column(name = "long_description")
    private String longDescription;

    @Column(name = "tags")
    private String tags; // comma-separated; null/empty = none

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
